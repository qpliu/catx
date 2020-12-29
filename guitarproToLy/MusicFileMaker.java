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
	print("\\include "+Stuff.escape(main.lyricsFileMaker.filename));
	print("\\include "+Stuff.escape(main.karaokeFileMaker.filename));
	for (int i=0; i<main.trackFileMaker.length; i++){
	    TrackFileMaker tfm=main.trackFileMaker[i];
	    if (tfm!=null)
		print("\\include "+Stuff.escape(tfm.filename));
	    TextFileMaker xfm=main.textFileMaker[i];
	    if (xfm!=null)
		print("\\include "+Stuff.escape(xfm.filename+"_midi"));
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
