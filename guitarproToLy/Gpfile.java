import java.io.*;
import java.util.*;

class Gpfile extends Gpinput{
    final Arg arg;
    int version;
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
    class Chord{
	String name;
	String ly;
	Chord read(Track track)throws IOException{
	    if (!readBoolean())
		readChord3f(track);
	    else if (version<400)
		readChord3t(track);
	    else
		readChord4(track);
	    return this;
	}
	private void readChord3f(Track track)throws IOException{
	    name = readIntSizeBlob().toByteSizeString();
	    readChordNotes(track,6);
	}
	private void readChord3t(Track track)throws IOException{
	    boolean sharp=readBoolean();
	    readBlob(3);
	    int root=readInt();
	    int type=readInt();
	    int extension=readInt();
	    int bass=readInt();
	    int tonality=readInt();
	    boolean add=readBoolean();
	    name = readBlob(23).toByteSizeString();
	    int fifth=readInt();
	    int ninth=readInt();
	    int eleventh=readInt();
	    readChordNotes(track,6);
	    int barresCount=readInt();
	    Blob barreFrets=readBlob(8);
	    Blob barreStarts=readBlob(8);
	    Blob barreEnds=readBlob(8);
	    Blob omissions=readBlob(7);
	    readByte();
	}
	private void readChord4(Track track)throws IOException{
	    boolean sharp=readBoolean();
	    readBlob(3);
	    int root=readByte();
	    int type=readByte();
	    int extension=readByte();
	    int bass=readInt();
	    int tonality=readInt();
	    boolean add=readBoolean();
	    name = readBlob(23).toByteSizeString();
	    int fifth=readByte();
	    int ninth=readByte();
	    int eleventh=readByte();
	    readChordNotes(track,7);
	    int barresCount=readByte();
	    Blob barreFrets=readBlob(5);
	    Blob barreStarts=readBlob(5);
	    Blob barreEnds=readBlob(5);
	    Blob omissions=readBlob(7);
	    readByte();
	    Blob fingerings=readBlob(7);
	    boolean show=readBoolean();
	}
	private void readChordNotes(Track track,int stringCount)throws IOException{
	    List<Integer>l=new ArrayList<Integer>();
	    int firstFret=readInt();
	    if (firstFret!=0)
		for (int i=0; i<stringCount; i++){
		    int fret=readInt();
		    if (fret>=0)
			l.add(track.tuning[i]+fret);
		}
	    Collections.sort(l);
	    if (l.size()==0)
		return;
	    StringBuilder sb=new StringBuilder();
	    for (Integer i:l)
		sb.append(' ').append(Stuff.midi2ly(i,arg));
	    ly = "<"+sb.append(">").substring(1);
	}
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
    class Slide{
	int type;
	Slide read()throws IOException{
	    if (version>=400)
		type = readByte();
	    return this;
	}
    }
    class GraceNote{
	int fret;
	int velocity;
	int duration;
	int transition;
	GraceNote read()throws IOException{
	    fret = readByte();
	    velocity = readByte();
	    if (version>=500){
		transition = readByte();
		duration = readByte();
		int bits=readByte();
	    }else{
		duration = readByte();
		transition = readByte();
	    }
	    return this;
	}
    }
    class Bend{
	double[]x;
	double[]y;
	boolean[]vibrato;
	Bend read()throws IOException{
	    int type=readByte();
	    int value=readInt();
	    x = new double[readInt()];
	    y = new double[x.length];
	    vibrato = new boolean[x.length];
	    for (int i=0; i<x.length; i++){
		x[i] = readInt()*12./60;
		y[i] = readInt()/25.;
		vibrato[i] = readBoolean();
	    }
	    return this;
	}
	Bend readTremoloBar()throws IOException{
	    int value=readInt();
	    x = new double[]{0,0,.5,-value/25.,1,0};
	    y = new double[]{0,0,.5,-value/25.,1,0};
	    vibrato = new boolean[3];
	    return this;
	}
    }
    class BeatEffects{
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
	BeatEffects read()throws IOException{
	    if (version<400)
		readBeatEffects3();
	    else
		readBeatEffects4();
	    return this;
	}
	private void readBeatEffects3()throws IOException{
	    int bits=readByte();
	    vibrato = (bits&1)!=0;
	    wideVibrato = (bits&2)!=0;
	    fadeIn = (bits&16)!=0;
	    if ((bits&32)!=0){
		slapEffect = readByte();
		tremoloBar = new Bend().readTremoloBar();
	    }
	    if ((bits&64)!=0)
		beatStroke = new BeatStroke().read();
	    natrualHarmonic = (bits&4)!=0;
	    artificialHarmonic = (bits&8)!=0;
	}
	private void readBeatEffects4()throws IOException{
	    int bits0=readByte();
	    int bits1=readByte();
	    wideVibrato = (bits0&2)!=0;
	    fadeIn = (bits0&16)!=0;
	    if ((bits0&32)!=0)
		slapEffect = readByte();
	    if ((bits1&4)!=0)
		tremoloBar = new Bend().read();
	    if ((bits0&64)!=0)
		beatStroke = new BeatStroke().read();
	    hasRasgueado = (bits1&1)!=0;
	    if ((bits1&2)!=0)
		pickStroke = readByte();
	}
    }
    class BeatStroke{
	int strokeDown;
	int strokeUp;
	BeatStroke read()throws IOException{
	    strokeDown = readByte();
	    strokeUp = readByte();
	    return this;
	}
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
    Gpfile(DataInputStream is,Arg arg){
	super(is);
	this.arg = arg;
    }
}
