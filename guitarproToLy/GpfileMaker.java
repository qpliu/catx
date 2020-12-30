import java.io.*;

final class GpfileMaker{
    static Gpfile make(DataInputStream is,Arg arg)throws IOException{
	Gpfile gpfile=new Gpfile(is,arg,0);
	String version=gpfile.readBlob(31).toByteSizeString();
	Log.info("version=%s",version);
	if (version.equals("FICHIER GUITAR PRO v3.00"))
	    gpfile = new Gp3file(is,arg,0x30000);
	if (version.equals("FICHIER GUITAR PRO v4.00"))
	    gpfile = new Gp4file(is,arg,0x40000);
	if (version.equals("FICHIER GUITAR PRO v5.00"))
	    gpfile = new Gp5file(is,arg,0x50000);
	if (version.equals("FICHIER GUITAR PRO v5.10"))
	    gpfile = new Gp5file(is,arg,0x51000);
	gpfile.parse();
	return gpfile;
    }
}
