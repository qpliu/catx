import java.io.*;
import java.util.*;

class Gp4file extends Gp3file{
    Gp4file(DataInputStream is)throws IOException{
	super(is);
    }
    void parse()throws IOException{
	if (!version.equals("FICHIER GUITAR PRO v4.00")){
	    super.parse();
	    return;
	}
    }
}
