import java.io.*;
import java.util.*;

final class Gp5file extends Gpfile{
    String tripletFeel;
    int time_n,time_d;
    MidiChannel[]midiChannels;
    final class MidiChannel{
	final int instrument;
	MidiChannel()throws IOException{
	    instrument = readInt();
	    double volume=readByte()/127.;
	    double balance=readByte()/127.;
	    double chorus=readByte()/127.;
	    double reverb=readByte()/127.;
	    double phaser=readByte()/127.;
	    double tremolo=readByte()/127.;
	    readByte();
	    readByte();
	}
    }
    Gp5file(DataInputStream is,Arg arg)throws IOException{
	super(is,arg);
	String version=readBlob(31).toByteSizeString();
	Log.info("version=%s",version);
	if (version.equals("FICHIER GUITAR PRO v3.00"))
	    this.version = 300;
	else if (version.equals("FICHIER GUITAR PRO v4.00"))
	    this.version = 400;
	else if (version.equals("CLIPBOARD GUITAR PRO 4.0 [c6]")){
	    this.version = 400;
	    readClipboard();
	}else if (version.equals("FICHIER GUITAR PRO v5.00"))
	    this.version = 500;
	else if (version.equals("FICHIER GUITAR PRO v5.10"))
	    this.version = 510;
	else
	    throw new IOException("Bad version");
	parse();
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
	if (version>=500){
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
	int startMeasure=readInt();
	int stopMeasure=readInt();
	int startTrack=readInt();
	int stopTrack=readInt();
	if (version>=500){
	    int startBeat=readInt();
	    int stopBeat=readInt();
	    Blob subbarCopy=readIntSizeBlob();
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
    void parse()throws IOException{
	readInfo();
	if (version<500)
	    readTripletFeel();
	readLyrics();
	readRSEMasterEffect();
	readPageSetup();
	if (version>=500){
	    String tempoName=readIntSizeBlob().toByteSizeString();
	}
	global_tempo = readInt();
	if (version>500){
	    boolean hideTemp0=readBoolean();
	}
	if (version<500)
	    global_key = new KeySignature(readInt(),0);
	else
	    global_key = new KeySignature(readByte(),0);
	if (version>=500){
	    int octave=readInt();
	}else if (version>=400){
	    int octave=readByte();
	}
	readMidiChannels();
	if (version>=500){
	    Blob directions=readBlob(2*19);
	    int reverb=readInt();
	}
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
	if (version>=500){
	    readByte();
	    if (version<=500)
		readByte();
	}
	for (int i=0; i<measures.length; i++)
	    for (int j=0; j<tracks.length; j++){
		readEvents(measures[i].time.add(arg.shift),measures[i],tracks[j]);
		if (version>=500 && (i!=measures.length-1 || j!=tracks.length-1)){
		    int linebreak=readByte();
		}
	    }
    }
    void readLyrics()throws IOException{
	if (version>=400){
	    int track=readInt();
	    for (int i=0; i<5; i++){
		TrackMeasureLyrics tml=new TrackMeasureLyrics();
		tml.which = String.valueOf(i);
		tml.track = track-1;
		tml.startingMeasure = readInt()-1;
		tml.lyrics = readIntSizeBlob().toString();
		Log.info("Lyrics which=%d track=%d measure=%d %s",i,tml.track,tml.startingMeasure,tml.lyrics);
		trackMeasureLyrics.add(tml);
	    }
	}
    }
    Measure readMeasure(int index)throws IOException{
	if (version>=500 && index!=0)
	    readByte();
	Measure measure=new Measure();
	measure.index = index;
	measure.name = String.valueOf(index+1);
	int bits=readUnsignedByte();
	if ((bits&1)!=0)
	    time_n = readUnsignedByte();
	if ((bits&2)!=0)
	    time_d = readUnsignedByte();
	if ((bits&4)!=0)
	    measure.repeatStart = true;
	if ((bits&8)!=0)
	    measure.repeatEnd = readUnsignedByte();
	if ((bits&16)!=0)
	    measure.repeatAlternate = readUnsignedByte();
	if ((bits&32)!=0){
	    measure.rehearsalMark = readIntSizeBlob().toByteSizeString();
	    int color=readInt();
	}
	if ((bits&64)!=0)
	    measure.key = new KeySignature(readByte(),readByte());
	if ((bits&128)!=0)
	    measure.hasDoubleBar = true;
	measure.tripletFeel = tripletFeel;
	measure.time_n = time_n;
	measure.time_d = time_d;
	if (version>=500){
	    if ((bits&3)!=0){
		Blob timeSignatureBeams=readBlob(4);
	    }
	    if ((bits&16)==0)
		readByte();
	    int tf=readByte();
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
	if (version>=500){
	    if (index==0 || version<=500)
		readByte();
	}
	int bits=readUnsignedByte();
	track.name = readBlob(41).toByteSizeString();
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
	if (version>=500){
	    int bits1=readShort();
	    int autoAccentuation=readByte();
	    int channelBank=readByte();
	    int rse_humanize=readByte();
	    readBlob(12);
	    readBlob(12);
	    readRSEInstrument();
	    if (version>500){
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
	if (version<=500){
	    int effectNumber=readShort();
	    readByte();
	}else{
	    int effectNumber=readInt();
	}
    }
    void readRSEInstrumentEffect()throws IOException{
	if (version>500){
	    String effect=readIntSizeBlob().toByteSizeString();
	    String category=readIntSizeBlob().toByteSizeString();
	}
    }
    void readRSEMasterEffect()throws IOException{
	if (version>500){
	    int volume=readInt();
	    readInt();
	    readBlob(11);
	}
    }
    void readEvents(Rational measureStartTime,Measure measure,Track track)throws IOException{
	int nvoices=version>=500?2:1;
	for (int voice=0; voice<nvoices; voice++){
	    Rational time=measureStartTime;
	    int nbeats=readInt();
	    for (int i=0; i<nbeats; i++){
		int bits=readUnsignedByte();
		Log.debug("measure %s track %d voice %d beat %d bits=0x%02x",measure.name,track.index,voice,i,bits);
		int status=1;
		if ((bits&64)!=0)
		    status = readUnsignedByte();
		Rational duration=new Rational(measure.time_d,1<<readByte()+2);
		if ((bits&1)!=0)
		    duration = duration.add(duration.divide(2));
		if ((bits&32)!=0){
		    int tuplet=readInt();
		    duration = duration.divide(tuplet);
		    for (; tuplet>1; tuplet>>=1)
			duration = duration.multiply(2);
		}
		if ((bits&2)!=0)
		    track.events.add(new ChordEvent(time,duration,new Chord().read(track)));
		if ((bits&4)!=0)
		    track.events.add(new LyricEvent(time,duration,readIntSizeBlob().toByteSizeString(),false,false,"text"));
		BeatEffects beatEffects=null;
		if ((bits&8)!=0)
		    beatEffects = new BeatEffects().read();
		if ((bits&16)!=0)
		    readMixTableChange(measure);
		int stringBits=readUnsignedByte();
		for (int string=0; string<7; string++)
		    if ((stringBits&64>>string)!=0){
			NoteEvent e=new NoteEvent(track,time,duration);
			e.string = string;
			e.voice = voice;
			e.beatEffects = beatEffects;
			readNoteBits(e);
			if (status==1)
			    track.events.add(e);
		    }
		if (version>=500){
		    int bits1=readShort();
		    if ((bits1&2048)!=0)
			readByte();
		}
		if (status!=0)
		    time = time.add(duration);
	    }
	    if (!time.equals(measureStartTime.add(measure.time_n)) && !time.equals(measureStartTime))
		throw new IOException("Weird measure end got="+time+" mst="+measureStartTime+" time_n="+measure.time_n);
	}
    }
    void readNoteBits(NoteEvent e)throws IOException{
	int bits=readUnsignedByte();
	e.is_ghost = (bits&4)!=0;
	if ((bits&32)!=0){
	    int type=readUnsignedByte();
	    e.is_rest = type==0;
	    e.is_tie = type==2;
	    e.is_dead = type==3;
	}else
	    e.is_rest = true;
	if (version<500 && (bits&1)!=0){
	    int duration=readByte();
	    int tuplet=readByte();
	}
	if ((bits&16)!=0){
	    int velocity=readByte();
	}
	if ((bits&32)!=0)
	    e.fret = readUnsignedByte();
	if ((bits&128)!=0){
	    int left_hand_finger=readUnsignedByte();
	    int right_hand_finger=readUnsignedByte();
	}
	if (version>=500){
	    if ((bits&1)!=0)
		readBlob(8);
	    int bits1=readByte();
	}
	if ((bits&8)!=0)
	    if (version<400)
		readNoteEffects3(e);
	    else
		readNoteEffects4(e);
    }
    void readNoteEffects3(NoteEvent e)throws IOException{
	int bits=readUnsignedByte();
	e.is_hammer = (bits&2)!=0;
	e.is_let_ring = (bits&8)!=0;
	if ((bits&1)!=0)
	    e.bend = new Bend().read();
	if ((bits&16)!=0)
	    e.graceNote = new GraceNote().read();
	if ((bits&4)!=0)
	    e.slide = new Slide().read();
    }
    void readNoteEffects4(NoteEvent e)throws IOException{
	int bits0=readUnsignedByte();
	int bits1=readUnsignedByte();
	e.is_hammer = (bits0&2)!=0;
	e.is_let_ring = (bits0&8)!=0;
	e.is_staccato = (bits1&1)!=0;
	e.is_palm_mute = (bits1&2)!=0;
	e.is_vibrato = (bits1&64)!=0;
	if ((bits0&1)!=0)
	    e.bend = new Bend().read();
	if ((bits0&16)!=0)
	    e.graceNote = new GraceNote().read();
	if ((bits1&4)!=0)
	    e.tremoloPicking = readByte();
	if ((bits1&8)!=0)
	    e.slide = new Slide().read();
	if ((bits1&16)!=0)
	    readHarmonic();
	if ((bits1&32)!=0){
	    int trill_fret=readByte();
	    int trill_value=readByte();
	}
    }
    void readMixTableChange(Measure measure)throws IOException{
	int instrument=readByte();
	if (version>=500){
	    readRSEInstrument();
	    if (version<=500)
		readByte();
	}
        int volume=readByte();
        int balance=readByte();
        int chorus=readByte();
        int reverb=readByte();
        int phaser=readByte();
        int tremolo=readByte();
	if (version>=500){
	    String temponame=readIntSizeBlob().toByteSizeString();
	}
        measure.tempo = readInt();
	if (volume>=0)
            readByte();
	if (balance>=0)
            readByte();
	if (chorus>=0)
            readByte();
	if (reverb>=0)
            readByte();
	if (phaser>=0)
            readByte();
	if (tremolo>=0)
            readByte();
	if (measure.tempo>=0){
            readByte();
	    if (version>500)
		readBoolean();
	}
	if (version>=400){
	    int bits=readByte();
	    if (version>=500){
		int wahEffect=readByte();
		readRSEInstrumentEffect();
	    }
	}
    }
    void readPageSetup()throws IOException{
	if (version>=500){
	    int size_w=readInt();
	    int size_h=readInt();
	    int margin_left=readInt();
	    int margin_right=readInt();
	    int margin_top=readInt();
	    int margin_bottom=readInt();
	    int sizeProportion=readInt();
	    int headerAndFooter=readShort();
	    String title=readIntSizeBlob().toByteSizeString();
	    String subtitle=readIntSizeBlob().toByteSizeString();
	    String artist=readIntSizeBlob().toByteSizeString();
	    String album=readIntSizeBlob().toByteSizeString();
	    String words=readIntSizeBlob().toByteSizeString();
	    String music=readIntSizeBlob().toByteSizeString();
	    String wordsAndMusic=readIntSizeBlob().toByteSizeString();
	    String copyright0=readIntSizeBlob().toByteSizeString();
	    String copyright1=readIntSizeBlob().toByteSizeString();
	    String pageNumber=readIntSizeBlob().toByteSizeString();
	}
    }
    void readHarmonic()throws IOException{
	int type=readByte();
	if (version>=500){
	    if (type==2)
		readBlob(2);
	    if (type==2)
		readByte();
	}
    }
}
