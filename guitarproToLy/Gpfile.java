import java.io.*;
import java.util.*;

class Gpfile{
    final DataInputStream is;
    final String version;
    String artist;
    String title;
    Measure[]measures;
    Track[]tracks;
    final List<Event>events=new ArrayList<Event>();
    static class Measure{
	String name;
	String tripletFeel;
	int tempo;
	int time_n,time_d;
	int key0,key1;
	boolean repeatStart;
	int repeatEnd;
	int repeatAlternate;
	boolean hasDoubleBar;
	String rehearsalMark;
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder();
	    sb.append(String.format("Measure %s",name));
	    sb.append(String.format(" tempo=%d",tempo));
	    sb.append(String.format(" tripletFeel=%s",tripletFeel));
	    sb.append(String.format(" rehearsalMark=%s",rehearsalMark));
	    sb.append(String.format(" time=%d/%d",time_n,time_d));
	    sb.append(String.format(" key=%d,%d",key0,key1));
	    return sb.toString();
	}
    }
    static class Chord{
	String name;
    }
    static class Event{
	final Rational time;
	final Rational duration;
	Event(Rational time,Rational duration){
	    this.time = time; 
	    this.duration = duration; 
	}
    }
    static class ChordEvent extends Event{
	final Chord chord;
	ChordEvent(Rational time,Rational duration,Chord chord){
	    super(time,duration);
	    this.chord = chord; 
	}
    }
    static class TextEvent extends Event{
	final String text;
	TextEvent(Rational time,Rational duration,String text){
	    super(time,duration);
	    this.text = text; 
	}
    }
    static class NoteEvent extends Event{
	boolean ghost_note;
	NoteEvent(Rational time,Rational duration){
	    super(time,duration);
	}
    }
    static class Track{
	int index;
	String name;
	boolean isDrums;
	int[]tuning;
	int instrument;
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder();
	    sb.append(String.format("Track %d",index));
	    sb.append(String.format(" name=%s",name));
	    if (isDrums)
		sb.append(" isDrums");
	    sb.append(String.format(" tuning=%s",Arrays.toString(tuning)));
	    sb.append(String.format(" instrument=%d",instrument));
	    return sb.toString();
	}
    }
    static final class Blob{
	final byte[]b;
	Blob(byte[]b){
	    this.b = b;
	}
	String toByteSizeString()throws IOException{
	    return new String(b,1,b[0]&255);
	}
    }
    final boolean readBoolean()throws IOException{
	return is.readByte()!=0;
    }
    final int readInt()throws IOException{
	return Integer.reverseBytes(is.readInt());
    }
    final Blob readBlob(int size)throws IOException{
	byte[]b=new byte[size];
	is.readFully(b);
	return new Blob(b);
    }
    final Blob readIntSizeBlob()throws IOException{
	return readBlob(readInt());
    }
    Gpfile(DataInputStream is)throws IOException{
	this.is = is;
	version = readBlob(31).toByteSizeString();
	Log.info("version=%s",version);
	parse();
    }
    void parse()throws IOException{
	throw new IOException("Bad version "+version);
    }
    void dumpIs()throws IOException{
	StringBuilder sb=new StringBuilder();
	StringBuilder sba=new StringBuilder();
	for (int i=0; i<32; i++){
	    int j=is.read();
	    if (j==-1)
		break;
	    sb.append(String.format(" %02x",j));
	    sba.append(j>=' ' && j<='~'?(char)j:'.');
	}
	Log.error("dumpIs %s %s",sb,sba);
    }
}
