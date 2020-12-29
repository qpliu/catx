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
	for (TrackFileMaker tfm:main.trackFileMaker)
	    if (tfm!=null)
		print("\\include "+Stuff.escape(tfm.filename));
	print();
	indent("layoutmusic = <<");
	unindent(">>");
	print();
	indent("midimusic = <<");
	print("\\markupStuff");
	unindent(">>");
    }
}
