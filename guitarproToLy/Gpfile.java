import java.io.*;
import java.util.*;

class Gpfile{
    final Arg arg;
    final DataInputStream is;
    final String version;
    String artist;
    String title;
    Measure[]measures;
    Track[]tracks;
    static class Measure{
	String name;
	Rational time;
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
	String ly;
    }
    static class Event implements Comparable<Event>,Cloneable{
	Rational time;
	Rational duration;
	boolean tie_rhs;
	boolean tie_lhs;
	Event(Rational time,Rational duration){
	    this.time = time; 
	    this.duration = duration; 
	}
	@Override public int compareTo(Event e){
	    return time.compareTo(e.time);
	}
	@Override public Event clone(){
	    try{
		return (Event)super.clone();
	    }catch (CloneNotSupportedException e){
		throw new RuntimeException(e);
	    }
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
	boolean is_ghost;
	boolean is_rest;
	boolean is_tie;
	boolean is_dead;
	boolean is_hammer;
	boolean is_let_ring;
	boolean is_staccato;
	boolean is_palm_mute;
	boolean is_vibrato;
	int string;
	int fret;
	int tremoloPicking;
	Bend bend;
	BeatEffects beatEffects;
	GraceNote graceNote;
	Slide slide;
	NoteEvent(Rational time,Rational duration){
	    super(time,duration);
	}
    }
    static class Slide{
	int type;
    }
    static class GraceNote{
	int fret;
	int velocity;
	int duration;
	int transition;
    }
    static class Bend{
	double[]xy;
    }
    static class BeatEffects{
	boolean vibrato;
	boolean wideVibrato;
	boolean fadeIn;
	int slapEffect;
	Bend tremoloBar;
	BeatStroke beatStroke;
	boolean natrualHarmonic;
	boolean artificialHarmonic;
	boolean hasRasgueado;
	int pickStroke;
    }
    static class BeatStroke{
	int strokeDown;
	int strokeUp;
    }
    static class Track{
	int index;
	String name;
	boolean isDrums;
	int[]tuning;
	int instrument;
	final List<Event>events=new ArrayList<Event>();
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
	@Override public String toString(){
	    return new String(b);
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
    Gpfile(DataInputStream is,Arg arg,String version)throws IOException{
	this.is = is;
	this.arg = arg;
	this.version = version;
    }
    void parse()throws IOException{
	throw new IOException("Bad version");
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
