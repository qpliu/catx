import java.io.*;
import java.util.*;

final class LyricsFileMaker extends TrackFileMaker{
    final boolean karaoke;
    LyricsFileMaker(Main main,Arg arg,boolean karaoke)throws IOException{
	super(main,arg,arg.partName,"",arg.partName);
	this.karaoke = karaoke;
    }
    void make()throws IOException{
	indent(lyname+" = \\new Lyrics \\lyricmode {");
	makeMeasures();
	unindent("}");
    }
}
