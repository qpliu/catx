import java.io.*;
import java.util.*;

class Gp3file extends Gpfile{
    String tripletFeel="";
    int tempo;
    int key0,key1;
    int time_n,time_d;
    final class MidiChannel{
	final int index;
	final int instrument;
	final float volume;
	MidiChannel(int index){
	    this.index = index;
	    instrument = readInt();
	    volume = is.readByte()/127.;
	    float balance=is.readByte()/127.;
	    float chorus=is.readByte()/127.;
	    float reverb=is.readByte()/127.;
	    float phaser=is.readByte()/127.;
	    float tremolo=is.readByte()/127.;
	    is.readByte();
	    is.readByte();
	}
    }
    MidiChannels[]midiChannels;
    Gp3file(DataInputStream is)throws IOException{
	super(is);
    }
    void readInfo()throws IOException{
	title = readIntSizeBlob().toByteSizeString();
	System.out.println("title="+title);
	String subtitle=readIntSizeBlob().toByteSizeString();
	System.out.println("subtitle="+subtitle);
	artist = readIntSizeBlob().toByteSizeString();
	System.out.println("artist="+artist);
	String album=readIntSizeBlob().toByteSizeString();
	System.out.println("album="+album);
	String words=readIntSizeBlob().toByteSizeString();
	System.out.println("words="+words);
	String copyright=readIntSizeBlob().toByteSizeString();
	System.out.println("copyright="+copyright);
	String tab=readIntSizeBlob().toByteSizeString();
	System.out.println("tab="+tab);
	String instructions=readIntSizeBlob().toByteSizeString();
	System.out.println("instructions="+instructions);
	int notesCount=readInt();
	for (int i=0; i<notesCount; i++){
	    String note=readIntSizeBlob().toByteSizeString();
	    System.out.println("note"+i+"="+note);
	}
    }
    void readMidiChannels()throws IOException{
	midiChannels = new MidiChannels[64];
	for (int i=0; i<midiChannels.length; i++)
	    midiChannels[i] = new MidiChannel(i);
    }
    void parse()throws IOException{
	if (!version.equals("FICHIER GUITAR PRO v3.00"))
	    throw new IOException("Bad version "+version);
	readInfo();
	if (readBoolean())
	    tripletFeel = "\\tripletFeel 8 ";
	tempo = readInt();
	key0 = readInt();
	key1 = 0;
	readMidiChannels();
	int measureCount=readInt();
	int trackCount=readInt();
	readMeasureHeaders(measureCount);
	readTracks(trackCount);
	readMeasures();
    }
    void readMeasureHeaders(int measureCount)throws IOException{
	for (int i=0; i<measureCount; i++){
	    Measure measure=new Measure();
	    int bits=is.readUnsignedByte();
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
	}
    }
    void readTracks(int trackCount)throws IOException{
    }
    void readMeasures()throws IOException{
    }
}
