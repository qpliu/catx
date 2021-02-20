import java.io.*;
import java.util.*;

final class MusicFileMaker extends FileMaker{
    MusicFileMaker(Main main)throws IOException{
	super(main,"music");
    }
    void make()throws IOException{
	indent("\\header {");
	if (main.gpfile.title!=null)
	    print("title = "+Stuff.quote(main.gpfile.title));
	if (main.gpfile.artist!=null)
	    print("composer = "+Stuff.quote(main.gpfile.artist));
	unindent("}");
	print();
	main.markupFileMaker.printInclude(this);
	for (TrackFileMaker tfm:main.trackFileMaker)
	    tfm.printInclude(this);
	print();
	indent("layoutmusic = <<");
	for (TrackFileMaker tfm:main.trackFileMaker)
	    tfm.layout(this);
	unindent(">>");
	print();
	indent("midimusic = <<");
	print("\\"+main.markupFileMaker.lyname);
	for (TrackFileMaker tfm:main.trackFileMaker)
	    tfm.midi(this);
	unindent(">>");
    }
}
