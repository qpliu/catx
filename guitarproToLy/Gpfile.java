import java.io.*;
import java.util.*;

class Gpfile{
    final DataInputStream is;
    final String version;
    String artist;
    String title;
    final List<Measure>measures=new ArrayList<Measure>();
    final List<Track>tracks=new ArrayList<Track>();
    static class Measure{
	String tripletFeel;
	int tempo;
	int time_n,time_d;
	int key0,key1;
	boolean repeatStart;
	int repeatEnd;
	int repeatAlternate;
	boolean hasDoubleBar;
	String rehearsalMark;
    }
    static class Track{
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
	System.out.println("version="+version);
	parse();
    }
    void parse()throws IOException{
	throw new IOException("Bad version "+version);
    }
}
