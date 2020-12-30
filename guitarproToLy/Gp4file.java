import java.io.*;
import java.util.*;

class Gp4file extends Gp3file{
    Gp4file(DataInputStream is,Arg arg,int version)throws IOException{
	super(is,arg,version);
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
	if (version>=0x50000){
	    if (type==2)
		readBlob(2);
	    if (type==2)
		is.readByte();
	}
    }
    @Override Slide readSlide()throws IOException{
	Slide slide=new Slide();
	slide.type = is.readByte();
	return slide;
    }
}
