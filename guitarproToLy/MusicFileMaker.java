import java.io.*;
import java.util.*;

final class MusicFileMaker extends FileMaker{
    MusicFileMaker(Main main)throws IOException{
	super(main,"music");
    }
    void make()throws IOException{
	indent("\\header {");
	print("title = "+Stuff.quote(main.gpfile.title));
	print("composer = "+Stuff.quote(main.gpfile.artist));
	unindent("}");
	print();
	print("\\include "+Stuff.quote(main.markupFileMaker.filename));
	for (int i=0; i<main.trackFileMaker.length; i++){
	    TrackFileMaker tfm=main.trackFileMaker[i];
	    if (tfm!=null)
		print("\\include "+Stuff.quote(tfm.filename));
	}
	print();
	indent("layoutmusic = <<");
	for (int i=0; i<main.trackFileMaker.length; i++){
	    TrackFileMaker tfm=main.trackFileMaker[i];
	    if (tfm!=null)
		tfm.layout(this);
	}
	unindent(">>");
	print();
	indent("midimusic = <<");
	print("\\"+main.markupFileMaker.lyname);
	for (int i=0; i<main.trackFileMaker.length; i++){
	    TrackFileMaker tfm=main.trackFileMaker[i];
	    if (tfm!=null)
		tfm.midi(this);
	}
	unindent(">>");
    }
}
