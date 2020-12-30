import java.io.*;
import java.util.*;

class Gp4file extends Gp3file{
    Gp4file(DataInputStream is,Arg arg)throws IOException{
	super(is,arg);
    }
    void parse()throws IOException{
	if (!version.equals("FICHIER GUITAR PRO v4.00")){
	    super.parse();
	    return;
	}
    }
}
