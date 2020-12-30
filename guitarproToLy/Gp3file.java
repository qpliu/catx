import java.io.*;
import java.util.*;

class Gp3file extends Gpfile{
    String tripletFeel;
    int tempo;
    int key0,key1;
    int time_n,time_d;
    MidiChannel[]midiChannels;
    final class MidiChannel{
	final int instrument;
	MidiChannel()throws IOException{
	    instrument = readInt();
	    double volume=is.readByte()/127.;
	    double balance=is.readByte()/127.;
	    double chorus=is.readByte()/127.;
	    double reverb=is.readByte()/127.;
	    double phaser=is.readByte()/127.;
	    double tremolo=is.readByte()/127.;
	    is.readByte();
	    is.readByte();
	}
    }
    Gp3file(DataInputStream is,Arg arg)throws IOException{
	super(is,arg);
    }
    void readInfo()throws IOException{
	title = readIntSizeBlob().toByteSizeString();
	Log.info("title=%s",title);
	String subtitle=readIntSizeBlob().toByteSizeString();
	Log.info("subtitle=%s",subtitle);
	artist = readIntSizeBlob().toByteSizeString();
	Log.info("artist=%s",artist);
	String album=readIntSizeBlob().toByteSizeString();
	Log.info("album=%s",album);
	String words=readIntSizeBlob().toByteSizeString();
	Log.info("words=%s",words);
	String copyright=readIntSizeBlob().toByteSizeString();
	Log.info("copyright=%s",copyright);
	String tab=readIntSizeBlob().toByteSizeString();
	Log.info("tab=%s",tab);
	String instructions=readIntSizeBlob().toByteSizeString();
	Log.info("instructions=%s",instructions);
	int notesCount=readInt();
	for (int i=0; i<notesCount; i++){
	    String note=readIntSizeBlob().toByteSizeString();
	    Log.info("note%d=%s",i,note);
	}
    }
    void readMidiChannels()throws IOException{
	midiChannels = new MidiChannel[64];
	for (int i=0; i<midiChannels.length; i++)
	    midiChannels[i] = new MidiChannel();
    }
    void parse()throws IOException{
	if (!version.equals("FICHIER GUITAR PRO v3.00")){
	    super.parse();
	    return;
	}
	readInfo();
	if (readBoolean())
	    tripletFeel = "\\tripletFeel 8 ";
	tempo = readInt();
	key0 = readInt();
	key1 = 0;
	readMidiChannels();
	measures = new Measure[readInt()];
	tracks = new Track[readInt()];
	for (int i=0; i<measures.length; i++)
	    measures[i] = readMeasure(i);
	for (int i=0; i<tracks.length; i++)
	    tracks[i] = readTrack(i);
	Rational time=arg.shift;
	for (int i=0; i<measures.length; i++){
	    for (int j=0; j<tracks.length; j++)
		readEvents(time,i,tracks[j]);
	    time = time.add(measures[i].time_n);
	}
    }
    Measure readMeasure(int index)throws IOException{
	Measure measure=new Measure();
	measure.name = String.valueOf(index+1);
	int bits=is.readUnsignedByte();
	Log.debug("bits=0x%02x",bits);
	if ((bits&1)!=0)
	    time_n = is.readUnsignedByte();
	if ((bits&2)!=0)
	    time_d = is.readUnsignedByte();
	if ((bits&4)!=0)
	    measure.repeatStart = true;
	if ((bits&8)!=0)
	    measure.repeatEnd = is.readUnsignedByte();
	if ((bits&16)!=0)
	    measure.repeatAlternate = is.readUnsignedByte();
	if ((bits&32)!=0){
	    measure.rehearsalMark = readIntSizeBlob().toByteSizeString();
	    int color=readInt();
	}
	if ((bits&64)!=0){
	    key0 = is.readUnsignedByte();
	    key1 = is.readUnsignedByte();
	}
	if ((bits&128)!=0)
	    measure.hasDoubleBar = true;
	measure.tripletFeel = tripletFeel;
	measure.tempo = tempo;
	measure.time_n = time_n;
	measure.time_d = time_d;
	measure.key0 = key0;
	measure.key1 = key1;
	Log.info("%s",measure);
	return measure;
    }
    Track readTrack(int index)throws IOException{
	Track track=new Track();
	track.index = index;
	int bits=is.readUnsignedByte();
	Log.debug("bits=0x%02x",bits);
	track.name = readBlob(41).toByteSizeString();
	if ((bits&1)!=0)
	    track.isDrums = true;
	int stringCount=readInt();
	Log.debug("stringCount=%d",stringCount);
	track.tuning = new int[7];
	for (int i=0; i<track.tuning.length; i++)
	    track.tuning[i] = readInt();
	int port=readInt();
	Log.debug("port=%d",port);
	int notesChannel=readInt();
	Log.debug("notesChannel=%d",notesChannel);
	int effectsChannel=readInt();
	Log.debug("effectsChannel=%d",effectsChannel);
	track.instrument = midiChannels[notesChannel-1].instrument;
	int fretCount=readInt();
	Log.debug("fretCount=%d",fretCount);
	int offset=readInt();
	Log.debug("offset=%d",offset);
	int color=readInt();
	Log.debug("color=%d",color);
	Log.info("%s",track);
	return track;
    }
    void readEvents(Rational time,int measure,Track track)throws IOException{
	int nbeats=readInt();
	Rational measureEnd=time.add(measures[measure].time_n);
	for (int i=0; i<nbeats; i++){
	    int bits=is.readUnsignedByte();
	    Log.debug("measure %d track %d beat %d bits=0x%02x",measure,track.index,i,bits);
	    int status=1;
	    if ((bits&64)!=0){
		status = is.readUnsignedByte();
		Log.debug("status=%d",status);
	    }
	    Rational duration=new Rational(measures[measure].time_d,1<<is.readByte()+2);
	    if ((bits&1)!=0)
		duration = duration.add(duration.divide(2));
	    if ((bits&32)!=0){
		int tuplet=readInt();
		duration = duration.divide(tuplet);
		for (; tuplet>1; tuplet>>=1)
		    duration = duration.multiply(2);
	    }
	    Log.debug("duration=%s",duration);
	    if ((bits&2)!=0)
		track.events.add(new ChordEvent(time,duration,readBoolean()?readGp4Chord():readGp3Chord()));
	    if ((bits&4)!=0)
		track.events.add(new TextEvent(time,duration,readIntSizeBlob().toByteSizeString()));
	    if ((bits&8)!=0)
		readBeatEffects();
	    if ((bits&16)!=0)
		readMixTableChange();
	    int stringBits=is.readUnsignedByte();
	    for (int string=0; string<7; string++)
		if ((stringBits&1<<string)!=0){
		    NoteEvent e=new NoteEvent(time,duration);
		    e.string = string;
		    int noteBits=is.readUnsignedByte();
		    if ((noteBits&4)!=0)
			e.is_ghost = true;
		    if ((noteBits&32)!=0){
			int type=is.readUnsignedByte();
			if (type==0)
			    e.is_rest = true;
			else if (type==2)
			    e.is_tie = true;
			else if (type==3)
			    e.is_dead = true;
		    }else
			e.is_rest = true;
		    if ((noteBits&1)!=0){
			int d=is.readByte();
			int t=is.readByte();
		    }
		    if ((noteBits&16)!=0){
			int velocity=is.readByte();
		    }
		    if ((noteBits&32)!=0)
			e.fret = is.readUnsignedByte();
		    if ((noteBits&128)!=0){
			int left_hand_finger=is.readUnsignedByte();
			int right_hand_finger=is.readUnsignedByte();
		    }
		    if ((noteBits&8)!=0){
			int effectBits=is.readUnsignedByte();
			if ((effectBits&2)!=0){
			    boolean hammer=true;
			}
			if ((effectBits&8)!=0){
			    boolean let_ring=true;
			}
			if ((effectBits&1)!=0){
			    int type=is.readByte();
			    int value=readInt();
			    int pointCount=readInt();
			    for (int j=0; j<pointCount; j++){
				int x=readInt();
				int y=readInt();
				boolean vibrato=readBoolean();
			    }
			}
			if ((effectBits&16)!=0){
			    int fret=is.readByte();
			    int velocity=is.readByte();
			    int dur=is.readByte();
			    int transition=is.readByte();
			}
			if ((effectBits&4)!=0)
			    e.is_slide = true;
		    }
		    if (status==1)
			track.events.add(e);
		}
	    if (status!=0)
		time = time.add(duration);
	}
	if (!time.equals(measureEnd))
	    throw new IOException("Weird measure end got="+time+" want="+measureEnd);
    }
    Chord readGp3Chord()throws IOException{
	Chord chord=new Chord();
	chord.name = readIntSizeBlob().toByteSizeString();
	int firstFret=readInt();
	if (firstFret!=0)
	    for (int i=0; i<6; i++)
		readInt();
	return chord;
    }
    Chord readGp4Chord()throws IOException{
	Chord chord=new Chord();
	boolean sharp=readBoolean();
	is.readByte();
	is.readByte();
	is.readByte();
	int root=readInt();
	int type=readInt();
	int extension=readInt();
	int bass=readInt();
	int tonality=readInt();
	boolean add=readBoolean();
	chord.name = readBlob(23).toByteSizeString();
	int fifth=readInt();
	int ninth=readInt();
	int eleventh=readInt();
	int firstFret=readInt();
	for (int i=0; i<6; i++)
	    readInt();
	int barresCount=readInt();
	int barreFrets0=readInt();
	int barreFrets1=readInt();
	int barreStarts0=readInt();
	int barreStarts1=readInt();
	int barreEnds0=readInt();
	int barreEnds1=readInt();
	for (int i=0; i<7; i++)
	    readBoolean();
	is.readByte();
	return chord;
    }
    void readBeatEffects()throws IOException{
	int bits=is.readByte();
	if ((bits&32)!=0){
	    int slap=is.readByte();
	    int tremoloBar=readInt();
	}
	if ((bits&64)!=0){
	    int strokeDown=is.readByte();
	    int strokeUp=is.readByte();
	}
    }
    void readMixTableChange()throws IOException{
	int instrument=is.readByte();
        int volume=is.readByte();
        int balance=is.readByte();
        int chorus=is.readByte();
        int reverb=is.readByte();
        int phaser=is.readByte();
        int tremolo=is.readByte();
        int tempo=readInt();
	if (volume>=0)
            is.readByte();
	if (balance>=0)
            is.readByte();
	if (chorus>=0)
            is.readByte();
	if (reverb>=0)
            is.readByte();
	if (phaser>=0)
            is.readByte();
	if (tremolo>=0)
            is.readByte();
	if (tempo>=0)
            is.readByte();
    }
}
