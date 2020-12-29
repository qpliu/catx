import java.io.*;
import java.util.*;

final class KaraokeFileMaker extends FileMaker{
    static final String lyname="karaokeLyrics";
    KaraokeFileMaker(Main main)throws IOException{
	super(main,"karaoke");
    }
    void make()throws IOException{
	indent(lyname+" = \\new Lyrics \\lyricmode {");
	unindent("}");
    }
}
