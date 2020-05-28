import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

final class MergeMidi{
    private static final int DIVISION=384;
    private static final int DRUMS_CHANNEL=10-1;
    private static final int BEND_MAX_SIZE=128;
    private static final double BEND_RANGE=24;
    private final Map<String,List<TextEvent>>textEvents=new HashMap<String,List<TextEvent>>();
    private final List<MetaEvent>metaEvents=new ArrayList<MetaEvent>();
    private final List<Event>events=new ArrayList<Event>();
    private final List<OutputEvent>outputEvents=new ArrayList<OutputEvent>();
    private final Set<TextEvent>unused=new HashSet<TextEvent>();
    private static final Pattern textPattern=Pattern.compile("([>!.-]*)(\\([^ ()]*\\))?(?:([a-zA-Z]+)=)?([a-zA-Z]+.*)");
    private static final Pattern whatParamPattern=Pattern.compile("([a-zA-Z]+)(.*)");
    private static final Pattern lyNotePattern=Pattern.compile("([a-g])((?:es|is)*)([',]*)");
    private class OutputEvent implements Comparable<OutputEvent>{
	final long time;
	final int outTrackIndex;
	final int one;
	final int[]bytes;
	OutputEvent(long time,int outTrackIndex,int one,int...bytes){
	    this.time = time;
	    this.outTrackIndex = outTrackIndex;
	    this.one = one;
	    this.bytes = bytes.clone();
	}
	@Override public int compareTo(OutputEvent e){
	    return Long.compare(time,e.time);
	}
    }
    private class Event implements Comparable<Event>{
	final long time;
	final int sort;
	final String id;
	final int outTrackIndex;
	Event(long time,int sort,String id,int outTrackIndex){
	    this.time = time;
	    this.sort = sort;
	    this.id = id;
	    this.outTrackIndex = outTrackIndex;
	}
	@Override public int compareTo(Event e){
	    return Long.compare(time,e.time)*2+Integer.compare(sort,e.sort);
	}
    }
    private String timeToStringAndPrintMap(PrintStream ps,long time){
	int numerator=1;
	int denominator=1;
	int microsecondsPerQuarterNote=500000;
	long seconds=0;
	long measure=0;
	Collections.sort(metaEvents);
	int i=0;
	int imeasure=0;
	for (long lastTime=0; lastTime<time;){
	    if (ps!=null && measure>=imeasure*4*DIVISION*numerator)
		ps.println(String.format("measure=%d seconds=%.3f divisions=%d",++imeasure,1e-6/DIVISION*seconds,lastTime));
	    long t=ps==null?time:lastTime+1;
	    if (i<metaEvents.size()){
		MetaEvent me=metaEvents.get(i);
		if (me.time<t)
		    t = me.time;
	    }
	    long delta=t-lastTime;
	    lastTime = t;
	    seconds += delta*microsecondsPerQuarterNote;
	    measure += delta*denominator;
	    if (i<metaEvents.size()){
		MetaEvent me=metaEvents.get(i);
		if (me.time==t){
		    i++;
		    if (me instanceof TempoEvent){
			microsecondsPerQuarterNote = ((TempoEvent)me).getMicrosecondsPerQuarterNote();
		    }else if (me instanceof TimeSignatureEvent){
			int n=((TimeSignatureEvent)me).getNumerator();
			int nn=numerator*n/BigInteger.valueOf(numerator).gcd(BigInteger.valueOf(n)).intValue();
			measure *= nn/numerator;
			denominator = nn/n<<((TimeSignatureEvent)me).getLogDenominator();
			numerator = nn;
		    }else
			throw new RuntimeException();
		}
	    }
	}
	StringBuilder sb=new StringBuilder();
	sb.append("divisions=").append(time);
	sb.append(" seconds=").append(1e-6/DIVISION*seconds);
	sb.append(" measure=").append(1+(double)measure/(4*DIVISION*numerator));
	return sb.toString();
    }
    private void printMeasureToTime(PrintStream ps,long maxTime){
	timeToStringAndPrintMap(ps,maxTime);
    }
    private String timeToString(long time){
	if (time==Long.MAX_VALUE)
	    return "Long.MAX_VALUE";
	return timeToStringAndPrintMap(null,time);
    }
    private static double note2Midi(String s)throws IOException{
	try{
	    return Double.parseDouble(s);
	}catch (NumberFormatException e){}
	Matcher m=lyNotePattern.matcher(s);
	if (!m.matches())
	    throw new IOException("Bad note "+s);
	double k;
	String a=m.group(1);
	if (a.equals("c"))
	    k = 48;
	else if (a.equals("d"))
	    k = 50;
	else if (a.equals("e"))
	    k = 52;
	else if (a.equals("f"))
	    k = 53;
	else if (a.equals("g"))
	    k = 55;
	else if (a.equals("a"))
	    k = 57;
	else if (a.equals("b"))
	    k = 59;
	else
	    throw new RuntimeException();
	a = m.group(2);
	for (int i=0; i<a.length(); i+=2)
	    if (a.substring(i,i+2).equals("is"))
		k++;
	    else if (a.substring(i,i+2).equals("es"))
		--k;
	    else if (a.substring(i,i+2).equals("ih"))
		k += .5;
	    else if (a.substring(i,i+2).equals("eh"))
		k -= .5;
	    else
		throw new RuntimeException();
	a = m.group(3);
	for (int i=0; i<a.length(); i++)
	    if (a.charAt(i)=='\'')
		k += 12;
	    else if (a.charAt(i)==',')
		k -= 12;
	    else
		throw new RuntimeException();
	return k;
    }
    private static String[]makeParam(String s){
	List<String>l=new ArrayList<String>();
	for (StringTokenizer st=new StringTokenizer(s,":"); st.hasMoreTokens(); l.add(st.nextToken()));
	return l.toArray(new String[l.size()]);
    }
    private class TextEvent extends Event{
	final TextEvent parent;
	final long stop;
	final String what;
	final String[]param;
	final String trackName;
	final Set<Double>matchKey;
	final boolean matchTimeAndStop;
	boolean done;
	final Map<String,Object>map=new HashMap<String,Object>();
	TextEvent(TextEvent parent,long start,String id,int outTrackIndex,long stop,String trackName,boolean matchTimeAndStop,Set<Double>matchKey,String what,String[]param)throws IOException{
	    super(start,0,id,outTrackIndex);
	    this.parent = parent==null?this:parent;
	    this.stop = stop;
	    this.trackName = trackName;
	    this.matchTimeAndStop = matchTimeAndStop;
	    this.matchKey = matchKey;
	    this.what = what;
	    this.param = param;
	}
	boolean matches(NoteEvent note){
	    if (!trackName.equals(note.trackName))
		return false;
	    if (stop<=note.time)
		return false;
	    if (time>=note.stop)
		return false;
	    if (matchTimeAndStop && (time!=note.time || stop!=note.stop))
		return false;
	    if (matchKey!=null && !matchKey.contains(note.key))
		return false;
	    return true;
	}
	boolean narrowerThan(TextEvent a){
	    if (matchTimeAndStop!=a.matchTimeAndStop)
		return matchTimeAndStop;
	    if (time!=a.time)
		return time>a.time;
	    if (stop!=a.stop)
		return stop<a.stop;
	    if ((matchKey==null)!=(a.matchKey==null))
		return matchKey!=null;
	    return false;
	}
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder("TextEvent");
	    sb.append(" start ").append(timeToString(time));
	    sb.append(" stop ").append(timeToString(stop));
	    sb.append(" whatparam=").append(what);
	    for (String p:param)
		sb.append(':').append(p);
	    sb.append(" trackName=").append(trackName);
	    sb.append(" matchKey=").append(matchKey);
	    sb.append(" matchTimeAndStop=").append(matchTimeAndStop);
	    return sb.toString();
	}
    }
    private class MetaEvent extends Event{
	final int what;
	final byte[]data;
	MetaEvent(long time,String id,int outTrackIndex,int what,byte[]data){
	    super(time,1,id,outTrackIndex);
	    this.what = what;
	    this.data = data.clone();
	}
    }
    private class TempoEvent extends MetaEvent{
	TempoEvent(long time,String id,int outTrackIndex,int what,byte[]data){
	    super(time,id,outTrackIndex,what,data);
//	    System.err.println("Tempo="+60e6/getMicrosecondsPerQuarterNote()+" time="+time);
	}
	int getMicrosecondsPerQuarterNote(){
	    return (data[0]&255)<<16|(data[1]&255)<<8|data[2]&255;
	}
    }
    private class TimeSignatureEvent extends MetaEvent{
	TimeSignatureEvent(long time,String id,int outTrackIndex,int what,byte[]data){
	    super(time,id,outTrackIndex,what,data);
	}
	int getNumerator(){
	    return data[0]&255;
	}
	int getLogDenominator(){
	    return data[1]&255;
	}
    }
    private class ProgramChangeEvent extends Event{
	final int program;
	ProgramChangeEvent(long time,String id,int outTrackIndex,int program){
	    super(time,2,id,outTrackIndex);
	    this.program = program;
	}
    }
    private class BendEvent extends Event{
	final int amount;
	BendEvent(long time,String id,int outTrackIndex,int amount){
	    super(time,2,id,outTrackIndex);
	    this.amount = amount;
	}
    }
    private class NoteEvent extends Event{
	final long stop;
	final String trackName;
	double key;
	double velocity;
	boolean percussion;
	NoteEvent(long start,String id,int outTrackIndex,long stop,String trackName,double key,double velocity,boolean percussion){
	    super(start,2,id,outTrackIndex);
	    this.stop = stop;
	    this.trackName = trackName;
	    this.key = key;
	    this.velocity = velocity;
	    this.percussion = percussion;
	}
    }
    private class TrackReader{
	private final DataInputStream dis;
	private final String id;
	private final int outTrackIndex;
	private boolean isTextTrack;
	private String trackName;
	private long time;
	private final long[]keyTime=new long[128];
	private final int[]keyVelocity=new int[128];
	private Map<Integer,String>textMap;
	TrackReader(DataInputStream dis,String id,int outTrackIndex){
	    this.dis = dis;
	    this.id = id;
	    this.outTrackIndex = outTrackIndex;
	    Arrays.fill(keyTime,-1);
	}
	private int readVlen()throws IOException{
	    for (int v=0;;){
		int i=dis.readByte();
		v = v<<7|i&127;
		if (i>=0)
		    return v;
	    }
	}
	private boolean readMeta(int what,byte[]data)throws IOException{
	    if (what==0){
		System.err.println("Sequence Number="+((data[0]&255)<<8|data[1]&255));
	    }else if (what==1){
//		System.err.println("text="+new String(data));
	    }else if (what==2){
		System.err.println("copyright="+new String(data));
	    }else if (what==3){
		trackName = new String(data);
//		System.err.println("track name="+trackName);
		int i=trackName.indexOf(':');
		if (i!=-1)
		    trackName = trackName.substring(0,i);
		if (trackName.endsWith("text")){
		    isTextTrack = true;
		    try (FileInputStream fis=new FileInputStream(trackName+"_notemap")){
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			for (int j; (j=fis.read())!=-1; baos.write(j));
			textMap = new HashMap<Integer,String>();
			for (StringTokenizer st=new StringTokenizer(baos.toString(),"\n"); st.hasMoreTokens();)
			    textMap.put(textMap.size(),st.nextToken());
		    }
		    trackName = trackName.substring(0,trackName.length()-4);
		}
	    }else if (what==4){
//		System.err.println("instrument name="+new String(data));
	    }else if (what==5){
		System.err.println("lyric="+new String(data));
	    }else if (what==6){
		System.err.println("marker="+new String(data));
	    }else if (what==7){
		System.err.println("cue point="+new String(data));
	    }else if (what==0x20){
		System.err.println("Channel prefix "+(data[0]&255));
	    }else if (what==0x2f)
		return true;
	    else if (what==0x51)
		metaEvents.add(new TempoEvent(time,id,outTrackIndex,what,data));
	    else if (what==0x54)
		System.err.println("SMTPE Offset");
	    else if (what==0x58)
		metaEvents.add(new TimeSignatureEvent(time,id,outTrackIndex,what,data));
	    else if (what==0x59){
//		System.err.println("Key Signature");
	    }else if (what==0x7f)
		System.err.println("Sequencer-Specific Meta-event");
	    return false;
	}
	private void expandTextEvent(TextEvent parent,Map<String,String>equalsMap,TextEvent te)throws IOException{
	    unused.add(parent);
	    if (equalsMap.containsKey(te.what))
		for (StringTokenizer st=new StringTokenizer(equalsMap.get(te.what),";"); st.hasMoreTokens();){
		    Matcher m=whatParamPattern.matcher(st.nextToken());
		    if (!m.matches())
			throw new IOException("Bad whatparam "+m);
		    expandTextEvent(parent,equalsMap,new TextEvent(parent,te.time,te.id,outTrackIndex,te.stop,te.trackName,te.matchTimeAndStop,te.matchKey,m.group(1),makeParam(m.group(2))));
		}
	    else
		textEvents.computeIfAbsent(te.trackName,x->new ArrayList<TextEvent>()).add(te);
	}
	void read()throws IOException{
	    List<TextEvent>noend=new ArrayList<TextEvent>();
	    Map<String,String>equalsMap=new HashMap<String,String>();
	    int channel=-1;
	    for (;;){
		int deltatime=readVlen();
		time += deltatime;
		int one=dis.readByte()&255;
		String idc=id+','+(one&15);
		if (one==0xf0 || one==0xf7){
		    byte[]data=new byte[readVlen()];
		    dis.readFully(data);
		    throw new IOException(String.format("Sysex event 0x%02x len=%d",one,data.length));
		}else if (one==0xff){
		    int two=dis.readByte()&255;
		    byte[]data=new byte[readVlen()];
		    dis.readFully(data);
		    if (readMeta(two,data))
			break;
		}else if ((one&0xe0)==0x80){
		    if (channel!=-1 && channel!=(one&15))
			throw new IOException("More than one channel in track.");
		    channel = one&15;
		    int kk=dis.readByte()&255;
		    int vv=dis.readByte()&255;
		    if (keyTime[kk]!=-1){
			if (isTextTrack){
			    long start=keyTime[kk];
			    long stop=time;
			    String text=textMap.get(kk);
			    Matcher m=textPattern.matcher(text);
			    if (!m.matches())
				throw new IOException("Bad text="+text);
			    String scope=m.group(1);
			    String keys=m.group(2);
			    String eq=m.group(3);
			    String wp=m.group(4);
			    if (eq!=null){
				equalsMap.put(eq,wp);
				wp = eq;
			    }
			    if (wp.indexOf(';')!=-1)
				throw new IOException(text);
			    m = whatParamPattern.matcher(wp);
			    if (!m.matches())
				throw new IOException("Bad text="+text);
			    String what=m.group(1);
			    String[]param=makeParam(m.group(2));
			    boolean matchTimeAndStop=scope.indexOf('.')!=-1;
			    Set<Double>matchKey;
			    if (keys!=null){
				matchKey = new HashSet<Double>();
				for (StringTokenizer st=new StringTokenizer(keys,"|()"); st.hasMoreTokens();)
				    matchKey.add(note2Midi(st.nextToken()));
			    }else
				matchKey = null;
			    if (scope.indexOf('-')!=-1)
				start = stop;
			    if (scope.indexOf("!")!=-1)
				for (int i=noend.size();;){
				    TextEvent te=noend.get(--i);
				    if (te.what.equals(what)){
					noend.remove(i);
					te = new TextEvent(null,te.time,te.id,outTrackIndex,start,te.trackName,te.matchTimeAndStop,te.matchKey,te.what,te.param);
					expandTextEvent(te,equalsMap,te);
					break;
				    }
				}
			    else{
				TextEvent te=new TextEvent(null,start,idc,outTrackIndex,stop,trackName,matchTimeAndStop,matchKey,what,param);
				if (scope.indexOf(">")!=-1)
				    noend.add(te);
				else
				    expandTextEvent(te,equalsMap,te);
			    }
			}else
			    events.add(new NoteEvent(keyTime[kk],idc,outTrackIndex,time,trackName,kk,keyVelocity[kk],(one&15)==DRUMS_CHANNEL));
			keyTime[kk] = -1;
		    }
		    if ((one&0xf0)==0x90 && vv!=0){
			keyTime[kk] = time;
			keyVelocity[kk] = vv;
		    }
		}else if ((one&0xf0)==0xa0){
		    int kk=dis.readByte()&255;
		    int ww=dis.readByte()&255;
		    throw new IOException(String.format("Aftertouch channel=%d kk=0x%02x ww=0x%02x",one&15,kk,ww));
		}else if ((one&0xf0)==0xb0){
		    int cc=dis.readByte()&255;
		    int nn=dis.readByte()&255;
		    if (cc==7 && nn==100)
			;
		    else
			throw new IOException(String.format("MIDI Channel Mode channel=%d cc=0x%02x nn=0x%02x",one&15,cc,nn));
		}else if ((one&0xf0)==0xc0){
		    int program=dis.readByte()&255;
		    events.add(new ProgramChangeEvent(time,idc,outTrackIndex,program));
//		    System.err.println("ProgramChange channel="+(one&15)+" program="+program+" time="+time);
		}else if ((one&0xf0)==0xd0){
		    int ww=dis.readByte()&255;
		    throw new IOException(String.format("Channel pressure channel=%d %d",one&15,ww));
		}else if ((one&0xf0)==0xe0){
		    int lsb=dis.readByte()&255;
		    int msb=dis.readByte()&255;
		    int value=(int)(((msb<<7|lsb)-0x2000)*2/BEND_RANGE+.5+0x2000);
		    events.add(new BendEvent(time,idc,outTrackIndex,value));
		}else
		    throw new IOException(String.format("Weird 0x%02x",one));
	    }
	    for (int i=0; i<128; i++)
		if (keyTime[i]!=-1)
		    throw new IOException("Missing note stop");
	    for (TextEvent te:noend){
		te = new TextEvent(null,te.time,te.id,outTrackIndex,Long.MAX_VALUE,te.trackName,te.matchTimeAndStop,te.matchKey,te.what,te.param);
		expandTextEvent(te,equalsMap,te);
	    }
	}
    }
    private class OutputEventMaker{
	private final OutputChannel[]outputChannels=new OutputChannel[16];
	private final Map<String,Integer>idToProgram=new HashMap<String,Integer>();
	private long worstAge=Long.MAX_VALUE;
	private class OutputChannel{
	    final int number;
	    String lastId;
	    long lastEventTime=Integer.MIN_VALUE;
	    int lastProgram=-1;
	    OutputChannel(int number){
		this.number = number;
		setRpn(0,0,(int)(BEND_RANGE*128+.5));
	    }
	    void add(long time,int outTrackIndex,int one,int...bytes){
		outputEvents.add(new OutputEvent(time,outTrackIndex,one|number,bytes));
	    }
	    void setRpn(long time,int n,int v){
		add(time,-1,0xb0,101,n>>7);
		add(time,-1,0xb0,100,n&127);
		add(time,-1,0xb0,6,v>>7);
		add(time,-1,0xb0,38,v&127);
		add(time,-1,0xb0,101,127);
		add(time,-1,0xb0,100,127);
	    }
	    void addNote(int program,NoteEvent note)throws IOException{
		lastId = note.id;
		lastEventTime = note.stop;
		if (program!=lastProgram){
		    lastProgram = program;
		    add(note.time,note.outTrackIndex,0xb0,0,program>>14);
		    add(note.time,note.outTrackIndex,0xb0,32,program>>7&127);
		    add(note.time,note.outTrackIndex,0xc0,program&127);
		}
		int ikey=(int)(note.key+.5);
		int bend=(int)(0x2000+.5+(note.key-ikey)*0x2000/BEND_RANGE);
		int v=Math.min((int)(note.velocity+.5),127);
		add(note.time,note.outTrackIndex,0x90,ikey,v);
		if (bend!=0){
		    add(note.time,note.outTrackIndex,0xe0,bend&127,bend>>7);
		    add(note.stop,note.outTrackIndex,0xe0,0,0x40);
		}
		add(note.stop,note.outTrackIndex,0x90,ikey,0);
	    }
	}
	private OutputChannel getOutputChannel(NoteEvent note){
	    if (note.percussion)
		return outputChannels[DRUMS_CHANNEL];
	    for (OutputChannel oc:outputChannels)
		if (oc.number!=DRUMS_CHANNEL && note.id.equals(oc.lastId))
		    return oc;
	    OutputChannel bestChannel=null;
	    for (OutputChannel oc:outputChannels)
		if (oc.number!=DRUMS_CHANNEL && (bestChannel==null || oc.lastEventTime<bestChannel.lastEventTime))
		    bestChannel = oc;
	    worstAge = Math.min(worstAge,note.time-bestChannel.lastEventTime);
	    return bestChannel;
	}
	private int parseProgram(String[]param){
	    int p=0;
	    for (int i=0; i<param.length; i++)
		p = p<<7|Integer.parseInt(param[i]);
	    return p;
	}
	private void addSlide(TextEvent te,OutputChannel oc,NoteEvent note,int program)throws IOException{
	    int count=te.param.length>=1?Integer.parseInt(te.param[0]):2;
	    int[]index=(int[])te.map.computeIfAbsent("index",x->new int[1]);
	    NoteEvent last=(NoteEvent)te.map.get("note");
	    if (index[0]==0){
		te.map.put("note",note);
		index[0]++;
		return;
	    }
	    long startTime=index[0]==1?last.time:(last.time+last.stop)/2;
	    long endTime=index[0]==count?note.stop:(note.time+note.stop)/2;
	    NoteEvent n=new NoteEvent(startTime,last.id,last.outTrackIndex,endTime,last.trackName,last.key,last.velocity,last.percussion);
	    double y0=0x2000;
	    double y1=0x2000+0x1fff/BEND_RANGE*(note.key-last.key);
	    for (int j=0; j<BEND_MAX_SIZE; j++){
		int bend=(int)(y0+j*(y1-y0)/(BEND_MAX_SIZE-1)+.5);
		oc.add(n.time+j*(n.stop-n.time)/(BEND_MAX_SIZE-1),n.outTrackIndex,0xe0,bend&127,bend>>7);
	    }
	    oc.addNote(program,n);
	    if (index[0]==count){
		index[0] = 0;
		oc.add(n.stop,n.outTrackIndex,0xe0,0,0x40);
	    }
	    te.map.put("note",note);
	}
	private void addBend(OutputChannel oc,NoteEvent note,double amplitude,double phase,double offset,double wl,long start,long stop){
	    phase *= Math.PI/180;
	    wl *= Math.PI/180;
	    amplitude *= 0x1fff;
	    int size=Math.min(BEND_MAX_SIZE,(int)(stop-start));
	    for (int i=1; i<size; i++){
		int bend=(int)(0x2000+.5+(Math.sin(phase+wl*i/(size-1))+offset)*amplitude);
		oc.add(start+i*(stop-start)/size,note.outTrackIndex,0xe0,bend&127,bend>>7);
	    }
	    oc.add(stop,note.outTrackIndex,0xe0,0,0x40);
	}
	private void sendToOc(String id,long time,int outTrackIndex,int one,int...bytes){
	    for (OutputChannel oc:outputChannels)
		if (oc.lastId.equals(id)){
		    oc.add(time,outTrackIndex,one,bytes);
		    break;
		}
	}
	void makeMetaEvents()throws IOException{
	    Collections.sort(metaEvents);
	    Map<Integer,byte[]>metaMap=new HashMap<Integer,byte[]>();
	    for (MetaEvent me:metaEvents){
		byte[]old=metaMap.get(me.what);
		if (old==null || !Arrays.equals(old,me.data)){
		    metaMap.put(me.what,me.data);
		    int[]bytes=new int[me.data.length+2];
		    bytes[0] = me.what;
		    bytes[1] = me.data.length;
		    for (int i=0; i<me.data.length; i++)
			bytes[2+i] = me.data[i];
		    outputEvents.add(new OutputEvent(me.time,-1,0xff,bytes));
		}
	    }
	}
	void makeEvents()throws IOException{
	    Collections.sort(events);
	    for (int i=0; i<outputChannels.length; i++)
		outputChannels[i] = new OutputChannel(i);
	    long maxTime=0;
	    for (Event e:events){
		maxTime = Math.max(maxTime,e.time);
		if (e instanceof ProgramChangeEvent)
		    idToProgram.put(e.id,((ProgramChangeEvent)e).program);
		else if (e instanceof BendEvent){
		    int amount=((BendEvent)e).amount;
		    sendToOc(e.id,e.time,e.outTrackIndex,0xe0,amount&127,amount>>7);
		}else{
		    NoteEvent note=(NoteEvent)e;
		    maxTime = Math.max(maxTime,note.stop);
		    Map<String,TextEvent>temap=new HashMap<String,TextEvent>();
		    List<TextEvent>tes=textEvents.get(note.trackName);
		    if (tes!=null)
			for (TextEvent te:tes)
			    if (te.matches(note)){
				TextEvent old=temap.get(te.what);
				if (old==null || te.narrowerThan(old))
				    temap.put(te.what,te);
			    }
		    for (TextEvent te:temap.values())
			unused.remove(te.parent);
		    int program=idToProgram.getOrDefault(note.id,0);
		    TextEvent te;
		    if ((te=temap.remove("volume"))!=null)
			note.velocity *= Double.parseDouble(te.param[0]);
		    if ((te=temap.remove("vol"))!=null)
			note.velocity *= Double.parseDouble(te.param[0]);
		    if ((te=temap.remove("program"))!=null)
			program = parseProgram(te.param);
		    if ((te=temap.remove("changeNote"))!=null)
			note.key = note2Midi(te.param[0]);
		    if ((te=temap.remove("percussion"))!=null)
			note.percussion = true;
		    if ((te=temap.remove("melodic"))!=null)
			note.percussion = false;
		    OutputChannel oc=getOutputChannel(note);
		    if ((te=temap.remove("bendBefore"))!=null && !te.done){
			double n=Double.parseDouble(te.param[0]);
			addBend(oc,note,n/BEND_RANGE,180,1,90,te.time,te.stop);
			te.done = true;
		    }
		    if ((te=temap.remove("bendAfter"))!=null && !te.done){
			double n=Double.parseDouble(te.param[0]);
			addBend(oc,note,n/BEND_RANGE,270,1,90,te.time,te.stop);
			te.done = true;
		    }
		    if ((te=temap.remove("bend"))!=null && !te.done){
			double amplitude=Double.parseDouble(te.param[0]);
			double phase=Double.parseDouble(te.param[1]);
			double offset=Double.parseDouble(te.param[2]);
			double wl=Double.parseDouble(te.param[3]);
			addBend(oc,note,amplitude/BEND_RANGE,phase,offset,wl,te.time,te.stop);
			te.done = true;
		    }
		    if ((te=temap.remove("slide"))!=null){
			addSlide(te,oc,note,program);
			continue;
		    }
		    if ((te=temap.remove("bendSongsterr"))!=null && !te.done){
			long x0=te.time;
			double y0=0x2000;
			for (int i=0; i<te.param.length; i+=2){
			    long x1=te.time+(long)(Double.parseDouble(te.param[i])/100*(te.stop-te.time)+.5);
			    double y1=0x2000+0x1fff/BEND_RANGE*Double.parseDouble(te.param[i+1])/50;
			    int size=Math.min(BEND_MAX_SIZE,(int)(x1-x0));
			    for (int j=0; j<size; j++){
				int bend=(int)(y0+j*(y1-y0)/size+.5);
				oc.add(x0+j*(x1-x0)/size,note.outTrackIndex,0xe0,bend&127,bend>>7);
			    }
			    x0 = x1;
			    y0 = y1;
			}
			oc.add(te.stop,note.outTrackIndex,0xe0,0,0x40);
			te.done = true;
		    }
		    if ((te=temap.remove("bendPL"))!=null && !te.done){
			long x0=te.time;
			double y0=0x2000;
			for (int i=0; i<te.param.length; i+=2){
			    long x1=te.time+(long)(Double.parseDouble(te.param[i])*(te.stop-te.time)+.5);
			    double y1=0x2000+0x1fff/BEND_RANGE*Double.parseDouble(te.param[i+1]);
			    int size=Math.min(BEND_MAX_SIZE,(int)(x1-x0));
			    for (int j=0; j<size; j++){
				int bend=(int)(y0+j*(y1-y0)/size+.5);
				oc.add(x0+j*(x1-x0)/size,note.outTrackIndex,0xe0,bend&127,bend>>7);
			    }
			    x0 = x1;
			    y0 = y1;
			}
			oc.add(te.stop,note.outTrackIndex,0xe0,0,0x40);
			te.done = true;
		    }
		    if ((te=temap.remove("collottava"))!=null){
			double shift=Double.parseDouble(te.param[0]);
			oc.addNote(program,new NoteEvent(note.time,note.id,note.outTrackIndex,note.stop,note.trackName,note.key+shift,note.velocity,note.percussion));
		    }
		    if ((te=temap.remove("glissando"))!=null){
			int n=Integer.parseInt(te.param[0]);
			int m=te.param.length>1?Integer.parseInt(te.param[1]):Math.abs(n);
			for (int i=0; i<m; i++){
			    long t0=note.time+i*(note.stop-note.time)/m;
			    long t1=note.time+(i+1)*(note.stop-note.time)/m;
			    NoteEvent a=new NoteEvent(t0,note.id,note.outTrackIndex,t1,note.trackName,note.key+(double)i*n/m,note.velocity,note.percussion);
			    if (i==m-1)
				note = a;
			    else
				oc.addNote(program,a);
			}
		    }
		    if ((te=temap.remove("glissandoA"))!=null)
			note.key += (note.time-te.time)*Double.parseDouble(te.param[0])/(te.stop-te.time);
		    if ((te=temap.remove("crescendo"))!=null){
			double v0=Double.parseDouble(te.param[0]);
			double v1=Double.parseDouble(te.param[1]);
			note.velocity *= v0+(note.time-te.time)*(v1-v0)/(te.stop-te.time);
		    }
		    if (temap.size()!=0)
			throw new IOException("Bad What "+temap.keySet());
		    oc.addNote(program,note);
		}
	    }
	    outputEvents.add(new OutputEvent(maxTime,-1,0xff,0x2f,0));
	    try (PrintStream ps=new PrintStream(new FileOutputStream("measureToTime"))){
		printMeasureToTime(ps,maxTime);
	    }
	    System.err.println("WorstAge="+worstAge);
	    for (TextEvent te:unused)
		System.err.println("Unused "+te);
	}
    }
    private void makeOutputEvents()throws IOException{
	OutputEventMaker oem=new OutputEventMaker();
	oem.makeMetaEvents();
	oem.makeEvents();
    }
    private void output(DataOutputStream dos,int outTrackIndex)throws IOException{
	Collections.sort(outputEvents);
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	long lastTime=0;
	for (OutputEvent oe:outputEvents){
	    if (oe.outTrackIndex!=-1 && oe.outTrackIndex!=outTrackIndex)
		continue;
	    long delta=(int)(oe.time-lastTime);
	    lastTime = oe.time;
	    if (delta<0 || delta>=1<<28)
		throw new RuntimeException();
	    if (delta>=1<<7){
		if (delta>=1<<14){
		    if (delta>=1<<21)
			baos.write((int)delta>>21|128);
		    baos.write((int)delta>>14|128);
		}
		baos.write((int)delta>>7|128);
	    }
	    baos.write((int)delta&127);
	    baos.write(oe.one);
	    for (int i=0; i<oe.bytes.length; i++)
		baos.write(oe.bytes[i]);
	}
	byte[]b=baos.toByteArray();
	dos.write("MTrk".getBytes());
	dos.writeInt(b.length);
	dos.write(b);
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
    private void read(DataInputStream dis,String id,int outTrackIndex)throws IOException{
	byte[]b=new byte[4];
	int trackNumber=0;
	for (;;){
	    try{
		dis.readFully(b,0,1);
	    }catch (EOFException e){
		return;
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
	    else if (type.equals("MTrk"))
		try (DataInputStream d=new DataInputStream(new ByteArrayInputStream(c))){
		    new TrackReader(d,id+','+trackNumber++,outTrackIndex).read();
		}
	    else
		System.err.println("type="+type);
	}
    }
    public static void main(String[]argv)throws Exception{
	MergeMidi mm=new MergeMidi();
	for (int i=0; i<argv.length; i++)
	    try (DataInputStream dis=new DataInputStream(new FileInputStream(argv[i]))){
		mm.read(dis,argv[i],i);
	    }
	mm.makeOutputEvents();
	try (DataOutputStream dos=new DataOutputStream(System.out)){
	    dos.write("MThd".getBytes());
	    dos.writeInt(6);
	    dos.writeShort(1);
	    dos.writeShort(argv.length);
	    dos.writeShort(DIVISION);
	    for (int i=0; i<argv.length; i++)
		mm.output(dos,i);
	}
    }
    private static class PrintInputStream extends FilterInputStream{
	PrintInputStream(InputStream in){
	    super(in);
	}
	@Override public int read()throws IOException{
	    int i=super.read();
	    if (i==-1)
		System.err.println("< EOF");
	    else
		System.err.println(String.format("< %02x",i));
	    return i;
	}
	@Override public int read(byte[]b,int off,int len)throws IOException{
	    int i=super.read(b,off,len);
	    if (i!=0){
		System.err.print("<");
		for (int j=0; j<i; j++)
		    System.err.print(String.format(" %02x",255&b[off+j]));
		System.err.println();
	    }
	    return i;
	}
    }
}
