import java.io.*;
import java.util.*;

class Gp4file extends Gp3file{
    Gp4file(DataInputStream is,Arg arg,String version)throws IOException{
	super(is,arg,version);
    }
    @Override void parse()throws IOException{
	readClipboard();
	readInfo();
	readTripletFeel();
	readLyrics();
	tempo = readInt();
	key0 = readInt();
	key1 = 0;
	int octave=is.readByte();
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
		readEvents(measures[i].time.add(arg.shift),i,tracks[j]);

    }
    void readClipboard()throws IOException{
	if (version.startsWith("CLIPBOARD")){
	    int startMeasure=readInt();
	    int stopMeasure=readInt();
	    int startTrack=readInt();
	    int stopTrack=readInt();
	}
    }
    void readLyrics()throws IOException{
	int trackChoice=readInt();
	for (int i=0; i<5; i++){
	    int startingMeasure=readInt();
	    String lyric=readIntSizeBlob().toString();
	    Log.info("Lyric=%s",lyric);
	}
    }
    @Override Chord readGp4Chord(Track track)throws IOException{
	Chord chord=new Chord();
	boolean sharp=readBoolean();
	readBlob(3);
	int root=is.readByte();
	int type=is.readByte();
	int extension=is.readByte();
	int bass=readInt();
	int tonality=readInt();
	boolean add=readBoolean();
	chord.name = readBlob(23).toByteSizeString();
	int fifth=is.readByte();
	int ninth=is.readByte();
	int eleventh=is.readByte();
	chord.ly = readChordNotes(track,7);
	int barresCount=is.readByte();
	Blob barreFrets=readBlob(5);
	Blob barreStarts=readBlob(5);
	Blob barreEnds=readBlob(5);
	Blob omissions=readBlob(7);
	is.readByte();
	Blob fingerings=readBlob(7);
	boolean show=readBoolean();
	return chord;
    }
    @Override BeatEffects readBeatEffects()throws IOException{
	BeatEffects beatEffects=new BeatEffects();
	int bits0=is.readByte();
	int bits1=is.readByte();
	beatEffects.wideVibrato = (bits0&2)!=0;
	beatEffects.fadeIn = (bits0&16)!=0;
	if ((bits0&32)!=0)
	    beatEffects.slapEffect = is.readByte();
	if ((bits1&4)!=0)
	    beatEffects.tremoloBar = readBend();
	if ((bits0&64)!=0)
	    beatEffects.beatStroke = readBeatStroke();
	beatEffects.hasRasgueado = (bits1&1)!=0;
	if ((bits1&2)!=0)
	    beatEffects.pickStroke = is.readByte();
	return beatEffects;
    }
    @Override void readMixTableChange()throws IOException{
	super.readMixTableChange();
	int bits=is.readByte();
    }
    @Override void readNoteEffects(NoteEvent e)throws IOException{
	int bits0=is.readUnsignedByte();
	int bits1=is.readUnsignedByte();
	e.is_hammer = (bits0&2)!=0;
	e.is_let_ring = (bits0&8)!=0;
	e.is_staccato = (bits1&1)!=0;
	e.is_palm_mute = (bits1&2)!=0;
	e.is_vibrato = (bits1&64)!=0;
	if ((bits0&1)!=0)
	    e.bend = readBend();
	if ((bits0&16)!=0)
	    e.graceNote = readGraceNote();
	if ((bits1&4)!=0)
	    e.tremoloPicking = is.readByte();
	if ((bits1&8)!=0)
	    e.slide = readSlide();
	if ((bits1&16)!=0)
	    readHarmonic();
	if ((bits1&32)!=0){
	    int trill_fret=is.readByte();
	    int trill_value=is.readByte();
	}
    }
    void readHarmonic()throws IOException{
	int type=is.readByte();
    }
    @Override Slide readSlide()throws IOException{
	Slide slide=new Slide();
	slide.type = is.readByte();
	return slide;
    }
}
