import java.io.*;
import java.util.*;

class Gp5file extends Gp4file{
    Gp5file(DataInputStream is,Arg arg,int version)throws IOException{
	super(is,arg,version);
    }
    @Override void parse()throws IOException{
	readInfo();
	readLyrics();
	readRSEMasterEffect();
	readPageSetup();
	String tempoName=readIntSizeBlob().toByteSizeString();
	tempo = readInt();
	if (version>0x500ff){
	    boolean hideTemp0=readBoolean();
	}
	key0 = is.readByte();
	key1 = 0;
	int octave=is.readInt();
	readMidiChannels();
	Blob directions=readBlob(2*19);
	int reverb=readInt();
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
	if (version>=0x50000){
	    is.readByte();
	    if (version<=0x500ff)
		is.readByte();
	}
	for (int i=0; i<measures.length; i++)
	    for (int j=0; j<tracks.length; j++){
		readEvents(measures[i].time.add(arg.shift),measures[i],tracks[j]);
		if (i!=measures.length-1 || j!=tracks.length-1){
		    int linebreak=is.readByte();
		}
	    }
    }
    void readPageSetup()throws IOException{
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
    void readRSEMasterEffect()throws IOException{
	if (version>0x500ff){
	    int volume=readInt();
	    readInt();
	    readBlob(11);
	}
    }
}
