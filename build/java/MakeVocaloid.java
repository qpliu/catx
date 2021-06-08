import java.io.*;
import java.util.*;

final class MakeVocaloid{
    private static final int DIVISION=384;
    private static final String HZ="44100";
    private static final String OUT_FILENAME="MakeVocaloid.mid";
    private static final String WAV_FILENAME="MakeVocaloid.wav";
    private final String who;
    private String lang="en-us";
    private final ByteArrayOutputStream baos=new ByteArrayOutputStream();
    private final DataOutputStream dos=new DataOutputStream(baos);
    private int last;
    private int time;
    private MakeVocaloid(String who)throws Exception{
	this.who = who;
	try (DataInputStream dis=new DataInputStream(System.in)){
	    extractMidiFile(dis);
	    try (DataOutputStream dos=new DataOutputStream(new FileOutputStream(OUT_FILENAME))){
		dos.write("MThd".getBytes());
		dos.writeInt(6);
		dos.writeShort(0);
		dos.writeShort(1);
		dos.writeShort(DIVISION);
		dos.write("MTrk".getBytes());
		dos.writeInt(baos.size());
		baos.writeTo(dos);
	    }
	    ProcessBuilder pb=new ProcessBuilder("ecantorix.pl","-v",lang,"-t","12","-r",HZ,"-c","../ecantorix-cache","-O","wav","-o",WAV_FILENAME,OUT_FILENAME);
	    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
	    pb.redirectErrorStream(true);
	    Process p=pb.start();
	    p.getOutputStream().close();
	    p.waitFor();
	}
    }
    private void readThd(DataInputStream dis)throws IOException{
	int format=dis.readShort();
	if (format!=0 && format!=1)
	    throw new IOException("Bad format="+format);
	int tracks=dis.readShort();
	int division=dis.readShort();
	if (division!=DIVISION)
	    throw new IOException("Bad division="+division);
    }
    private void writeVlen(int len)throws IOException{
	writeVlen(len,0);
    }
    private void writeVlen(int len,int bit)throws IOException{
	if (len>127)
	    writeVlen(len>>7,128);
	dos.write(len&127|bit);
    }
    private void writeTime()throws IOException{
	writeVlen(time-last);
	last = time;
    }
    private static int readVlen(DataInputStream dis)throws IOException{
	for (int v=0;;){
	    int i=dis.readByte();
	    v = v<<7|i&127;
	    if (i>=0)
		return v;
	}
    }
    private boolean extractTrack(byte[]trackData)throws IOException{
	try (DataInputStream dis=new DataInputStream(new ByteArrayInputStream(trackData))){
	    last = 0;
	    time = 0;
	    boolean trackGood=false;
	    int[]chan2note=new int[16];
	    Arrays.fill(chan2note,-1);
	    for (;;){
		time += readVlen(dis);
		int one=dis.readUnsignedByte();
		if (one==0xff){
		    int what=dis.readUnsignedByte();
		    byte[]data=new byte[readVlen(dis)];
		    dis.readFully(data);
		    if (what==3){
			String trackName=new String(data);
			trackGood = trackName.equals(who);
		    }else if (what==5){
			StringBuilder sb=new StringBuilder();
			for (StringTokenizer st=new StringTokenizer(new String(data),"|"); st.hasMoreTokens();){
			    String l=st.nextToken();
			    if (l.startsWith("!lang="))
				lang = l.substring(6);
			    else if (!l.startsWith("!")){
				if (l.startsWith("(") && l.endsWith(")"))
				    l = "[["+l.substring(1,l.length()-1)+"]]";
				data = l.getBytes();
				writeTime();
				dos.write(one);
				dos.write(what);
				writeVlen(data.length);
				dos.write(data);
			    }
			}
		    }else{
			writeTime();
			dos.write(one);
			dos.write(what);
			writeVlen(data.length);
			dos.write(data);
			if (what==0x2f)
			    break;
		    }
		}else if ((one&0xe0)==0x80){
		    int kk=dis.readUnsignedByte();
		    int vv=dis.readUnsignedByte();
		    for (int ch=0; ch<16; ch++)
			if ((one&0xf0)==0x80 || vv==0){
			    if (chan2note[ch]==kk){
				writeTime();
				dos.write(0x80|ch);
				dos.write(kk);
				dos.write(0);
				chan2note[ch] = -1;
				break;
			    }
			}else if (ch!=10 && chan2note[ch]==-1){
			    chan2note[ch] = kk;
			    writeTime();
			    dos.write(0x90|ch);
			    dos.write(kk);
			    dos.write(0x80);
			    break;
			}
		}else if ((one&0xf0)==0xb0){
		    int cc=dis.readUnsignedByte();
		    int nn=dis.readUnsignedByte();
		}else if ((one&0xf0)==0xc0){
		    int program=dis.readUnsignedByte();
		}else if ((one&0xf0)==0xe0){
		    int lsb=dis.readUnsignedByte();
		    int msb=dis.readUnsignedByte();
		}else
		    throw new IOException(String.format("Weird 0x%02x",one));
	    }
	    return trackGood;
	}
    }
    private void extractMidiFile(DataInputStream dis)throws IOException{
	byte[]b=new byte[4];
	for (;;){
	    try{
		dis.readFully(b,0,1);
	    }catch (EOFException e){
		break;
	    }
	    dis.readFully(b,1,3);
	    String type=new String(b,0,4);
	    int len=dis.readInt();
	    byte[]c=new byte[len];
	    dis.readFully(c);
	    if (type.equals("MThd"))
		try (DataInputStream d=new DataInputStream(new ByteArrayInputStream(c))){
		    readThd(d);
		}
	    else if (type.equals("MTrk")){
		if (extractTrack(c))
		    break;
		baos.reset();
	    }else
		System.err.println("type="+type);
	}
    }
    public static void main(String[]argv)throws Exception{
	if (argv.length!=1){
	    System.err.println("java MakeVocaloid midiVocaloid|midiVocaloidTwo");
	    System.exit(1);
	}
	new MakeVocaloid(argv[0]);
    }
}
