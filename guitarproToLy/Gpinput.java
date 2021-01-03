import java.io.*;
import java.util.*;

class Gpinput{
    final DataInputStream is;
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
    Gpinput(DataInputStream is){
	this.is = is;
    }
    final boolean readBoolean()throws IOException{
	return is.readByte()!=0;
    }
    final int readInt()throws IOException{
	return Integer.reverseBytes(is.readInt());
    }
    final int readByte()throws IOException{
	return is.readByte();
    }
    final int readUnsignedByte()throws IOException{
	return is.readUnsignedByte();
    }
    final int readShort()throws IOException{
	return Short.reverseBytes(is.readShort());
    }
    final Blob readBlob(int size)throws IOException{
	byte[]b=new byte[size];
	is.readFully(b);
	return new Blob(b);
    }
    final Blob readIntSizeBlob()throws IOException{
	return readBlob(readInt());
    }
    final void dumpIs()throws IOException{
	for (;;){
	    StringBuilder sb=new StringBuilder();
	    StringBuilder sba=new StringBuilder();
	    int j=0;
	    for (int i=0; i<32; i++){
		j = is.read();
		if (j==-1)
		    break;
		sb.append(String.format(" %02x",j));
		sba.append(j>=' ' && j<='~'?(char)j:'.');
	    }
	    Log.error("dumpIs %s %s",sb,sba);
	    if (j==-1)
		System.exit(1);
	}
    }
}
