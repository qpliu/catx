import java.io.*;
import java.util.*;

final class MusicFileMaker extends FileMaker{
    MusicFileMaker(Main main)throws IOException{
	super(main,"music");
    }
    void make()throws IOException{
	indent("\\header {");
	print("title = "+Stuff.escape(main.gpfile.title));
	print("composer = "+Stuff.escape(main.gpfile.artist));
	unindent("}");
	print();
	print("\\include "+Stuff.escape(main.markupFileMaker.filename));
	for (int i=0; i<main.trackFileMaker.length; i++){
	    TrackFileMaker tfm=main.trackFileMaker[i];
	    if (tfm!=null)
		print("\\include "+Stuff.escape(tfm.filename));
	}
	print();
	indent("layoutmusic = <<");
	unindent(">>");
	print();
	indent("midimusic = <<");
	print("\\"+main.markupFileMaker.lyname);
	unindent(">>");
    }
}
