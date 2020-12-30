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
    Gp3file(DataInputStream is,Arg arg,int version)throws IOException{
	super(is,arg,version);
    }
    void readInfo()throws IOException{
	readClipboard();
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
	if (version>=0x50000){
	    String music=readIntSizeBlob().toByteSizeString();
	    Log.info("music=%s",music);
	}
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
    void readClipboard()throws IOException{
	if ((version&VERSION_CLIPBOARD)!=0){
	    int startMeasure=readInt();
	    int stopMeasure=readInt();
	    int startTrack=readInt();
	    int stopTrack=readInt();
	    if (version>=0x50000){
		int startBeat=readInt();
		int stopBeat=readInt();
		Blob subbarCopy=readIntSizeBlob();
	    }
	}
    }
    void readMidiChannels()throws IOException{
	midiChannels = new MidiChannel[64];
	for (int i=0; i<midiChannels.length; i++)
	    midiChannels[i] = new MidiChannel();
    }
    void readTripletFeel()throws IOException{
	if (readBoolean())
	    tripletFeel = "\\tripletFeel 8 ";
    }
    @Override void parse()throws IOException{
	readInfo();
	readTripletFeel();
	readLyrics();
	tempo = readInt();
	key0 = readInt();
	key1 = 0;
	if (version>=0x40000){
	    int octave=is.readByte();
	}
	readMidiChannels();
	measures = new Measure[readInt()];
	tracks = new Track[readInt()];
	Rational time=Rational.ZERO;
	for (int i=0; i<measures.length; i++){
	    measures[i] = readMeasure(i);
	    measures[i].time = time;
	    time = time.add(measures[i].time_n);
	}
	for (int i=0; i<tracks.length; i++)
	    tracks[i] = readTrack(i);
	for (int i=0; i<measures.length; i++)
	    for (int j=0; j<tracks.length; j++)
		readEvents(measures[i].time.add(arg.shift),measures[i],tracks[j]);
    }
    void readLyrics()throws IOException{
	if (version>=0x40000){
	    int trackChoice=readInt();
	    for (int i=0; i<5; i++){
		int startingMeasure=readInt();
		String lyric=readIntSizeBlob().toString();
		Log.info("Lyric=%s",lyric);
	    }
	}
    }
    Measure readMeasure(int index)throws IOException{
	if (version>=0x50000 && index!=0)
	    is.readByte();
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
	    key0 = is.readByte();
	    key1 = is.readByte();
	}
	if ((bits&128)!=0)
	    measure.hasDoubleBar = true;
	measure.tripletFeel = tripletFeel;
	measure.tempo = tempo;
	measure.time_n = time_n;
	measure.time_d = time_d;
	measure.key0 = key0;
	measure.key1 = key1;
	if (version>=0x50000){
	    if ((bits&3)!=0){
		Blob timeSignatureBeams=readBlob(4);
	    }
	    if ((bits&16)==0)
		is.readByte();
	    int tf=is.readByte();
	    if (tf==1)
		measure.tripletFeel = "\\tripletFeel 8 ";
	    else if (tf==2)
		measure.tripletFeel = "\\tripletFeel 16 ";
	    else
		measure.tripletFeel = null;
	}
	Log.info("%s",measure);
	return measure;
    }
    Track readTrack(int index)throws IOException{
	Track track=new Track();
	track.index = index;
	if (version>=0x50000){
	    if (index==0 || version<=0x500ff)
		is.readByte();
	}
	int bits=is.readUnsignedByte();
	Log.debug("bits=0x%02x",bits);
	track.name = readBlob(41).toByteSizeString();
	Log.debug("name=%s",track.name);
	if ((bits&1)!=0)
	    track.isDrums = true;
	int stringCount=readInt();
	Log.debug("stringCount=%d",stringCount);
	track.tuning = new int[stringCount];
	for (int i=0; i<7; i++){
	    int t=readInt();
	    if (i<stringCount)
		track.tuning[i] = t;
	}
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
	if (version>=0x50000){
	    int bits1=readShort();
	    int autoAccentuation=is.readByte();
	    int channelBank=is.readByte();
	    int rse_humanize=is.readByte();
	    readBlob(12);
	    readBlob(12);
	    readRSEInstrument();
	    if (version>0x500ff){
		Blob equalizer=readBlob(4);
		readRSEInstrumentEffect();
	    }
	}
	Log.info("%s",track);
	return track;
    }
    void readRSEInstrument()throws IOException{
	int instrument=readInt();
	readInt();
	int soundbank=readInt();
	if (version<=0x500ff){
	    int effectNumber=readShort();
	    is.readByte();
	}else{
	    int effectNumber=readInt();
	}
    }
    void readRSEInstrumentEffect()throws IOException{
	if (version>0x500ff){
	    String effect=readIntSizeBlob().toByteSizeString();
	    String category=readIntSizeBlob().toByteSizeString();
	}
    }
    void readEvents(Rational measureStartTime,Measure measure,Track track)throws IOException{
	int nvoices=version>=0x50000?2:1;
	for (int voice=0; voice<nvoices; voice++){
	    Rational time=measureStartTime;
	    int nbeats=readInt();
	    Log.debug("nbeats=%d",nbeats);
	    for (int i=0; i<nbeats; i++){
		int bits=is.readUnsignedByte();
		Log.debug("measure %s track %d voice %d beat %d bits=0x%02x",measure.name,track.index,voice,i,bits);
		int status=1;
		if ((bits&64)!=0){
		    status = is.readUnsignedByte();
		    Log.debug("status=%d",status);
		}
		Rational duration=new Rational(measure.time_d,1<<is.readByte()+2);
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
		    track.events.add(new ChordEvent(time,duration,readBoolean()?readGp4Chord(track):readGp3Chord(track)));
		if ((bits&4)!=0)
		    track.events.add(new TextEvent(time,duration,readIntSizeBlob().toByteSizeString()));
		BeatEffects beatEffects=null;
		if ((bits&8)!=0)
		    beatEffects = readBeatEffects();
		if ((bits&16)!=0)
		    readMixTableChange();
		int stringBits=is.readUnsignedByte();
		for (int string=0; string<7; string++)
		    if ((stringBits&64>>string)!=0){
			NoteEvent e=new NoteEvent(time,duration);
			e.string = string;
			e.beatEffects = beatEffects;
			readNoteBits(e);
			if (status==1)
			    track.events.add(e);
		    }
		if (version>=0x50000){
		    int bits1=readShort();
		    if ((bits1&2048)!=0)
			is.readByte();
		}
		if (status!=0)
		    time = time.add(duration);
	    }
	    if (!time.equals(measureStartTime.add(measure.time_n)) && !time.equals(measureStartTime))
		throw new IOException("Weird measure end got="+time+" mst="+measureStartTime+" time_n="+measure.time_n);
	}
    }
    void readNoteBits(NoteEvent e)throws IOException{
	int bits=is.readUnsignedByte();
	e.is_ghost = (bits&4)!=0;
	if ((bits&32)!=0){
	    int type=is.readUnsignedByte();
	    e.is_rest = type==0;
	    e.is_tie = type==2;
	    e.is_dead = type==3;
	}else
	    e.is_rest = true;
	if (version<0x50000 && (bits&1)!=0){
	    int duration=is.readByte();
	    int tuplet=is.readByte();
	}
	if ((bits&16)!=0){
	    int velocity=is.readByte();
	}
	if ((bits&32)!=0)
	    e.fret = is.readUnsignedByte();
	if ((bits&128)!=0){
	    int left_hand_finger=is.readUnsignedByte();
	    int right_hand_finger=is.readUnsignedByte();
	}
	if (version>=0x50000){
	    if ((bits&1)!=0)
		readBlob(8);
	    int bits1=is.readByte();
	}
	if ((bits&8)!=0)
	    readNoteEffects(e);
    }
    void readNoteEffects(NoteEvent e)throws IOException{
	int bits=is.readUnsignedByte();
	e.is_hammer = (bits&2)!=0;
	e.is_let_ring = (bits&8)!=0;
	if ((bits&1)!=0)
	    e.bend = readBend();
	if ((bits&16)!=0)
	    e.graceNote = readGraceNote();
	if ((bits&4)!=0)
	    e.slide = readSlide();
    }
    Slide readSlide()throws IOException{
	Slide slide=new Slide();
	if (version>=0x50000)
	    is.readByte();
	return slide;
    }
    GraceNote readGraceNote()throws IOException{
	GraceNote graceNote=new GraceNote();
	graceNote.fret = is.readByte();
	graceNote.velocity = is.readByte();
	if (version>=0x50000){
	    graceNote.transition = is.readByte();
	    graceNote.duration = is.readByte();
	    int bits=is.readByte();
	}else{
	    graceNote.duration = is.readByte();
	    graceNote.transition = is.readByte();
	}
	return graceNote;
    }
    String readChordNotes(Track track,int stringCount)throws IOException{
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
	    return null;
	StringBuilder sb=new StringBuilder();
	for (Integer i:l)
	    sb.append(' ').append(Stuff.midi2ly(i,arg));
	return "<"+sb.append(">").substring(1);
    }
    final Chord readGp3Chord(Track track)throws IOException{
	Chord chord=new Chord();
	chord.name = readIntSizeBlob().toByteSizeString();
	chord.ly = readChordNotes(track,6);
	return chord;
    }
    Chord readGp4Chord(Track track)throws IOException{
	Chord chord=new Chord();
	boolean sharp=readBoolean();
	readBlob(3);
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
	chord.ly = readChordNotes(track,6);
	int barresCount=readInt();
	Blob barreFrets=readBlob(8);
	Blob barreStarts=readBlob(8);
	Blob barreEnds=readBlob(8);
	Blob omissions=readBlob(7);
	is.readByte();
	return chord;
    }
    Bend readTremoloBar()throws IOException{
	Bend bend=new Bend();
	int value=readInt();
	bend.xy = new double[]{0,0,.5,-value/25.,1,0};
	return bend;
    }
    BeatEffects readBeatEffects()throws IOException{
	BeatEffects beatEffects=new BeatEffects();
	int bits=is.readByte();
	beatEffects.vibrato = (bits&1)!=0;
	beatEffects.wideVibrato = (bits&2)!=0;
	beatEffects.fadeIn = (bits&16)!=0;
	if ((bits&32)!=0){
	    beatEffects.slapEffect = is.readByte();
	    beatEffects.tremoloBar = readTremoloBar();
	}
	if ((bits&64)!=0)
	    beatEffects.beatStroke = readBeatStroke();
	beatEffects.natrualHarmonic = (bits&4)!=0;
	beatEffects.artificialHarmonic = (bits&8)!=0;
	return beatEffects;
    }
    BeatStroke readBeatStroke()throws IOException{
	BeatStroke beatStroke=new BeatStroke();
	beatStroke.strokeDown=is.readByte();
	beatStroke.strokeUp=is.readByte();
	return beatStroke;
    }
    void readMixTableChange()throws IOException{
	int instrument=is.readByte();
	if (version>=0x50000){
	    readRSEInstrument();
	    if (version<=0x500ff)
		is.readByte();
	}
        int volume=is.readByte();
        int balance=is.readByte();
        int chorus=is.readByte();
        int reverb=is.readByte();
        int phaser=is.readByte();
        int tremolo=is.readByte();
	if (version>=0x50000){
	    String temponame=readIntSizeBlob().toByteSizeString();
	}
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
	if (tempo>=0){
            is.readByte();
	    if (version>0x500ff)
		readBoolean();
	}
	if (version>=0x40000){
	    int bits=is.readByte();
	    if (version>=0x50000){
		int wahEffect=is.readByte();
		readRSEInstrumentEffect();
	    }
	}
    }
    Bend readBend()throws IOException{
	Bend bend=new Bend();
	int type=is.readByte();
	int value=readInt();
	bend.xy = new double[2*readInt()];
	for (int j=0; j<bend.xy.length; j+=2){
	    bend.xy[j] = readInt()*12./60;
	    bend.xy[j+1] = readInt()/25.;
	    boolean vibrato=readBoolean();
	}
	return bend;
    }
}
