import java.io.*;
import java.util.*;

class Gp5file extends Gp4file{
    Gp5file(DataInputStream is)throws IOException{
	super(is);
    }
    void parse()throws IOException{
	if (!version.equals("FICHIER GUITAR PRO v5.00")){
	    super.parse();
	    return;
	}
    }
}
