import java.io.*;
import java.util.*;

final class LyricsFileMaker extends FileMaker{
    static final String lyname="vocalsLyrics";
    LyricsFileMaker(Main main)throws IOException{
	super(main,"lyrics");
    }
    void make()throws IOException{
	indent(lyname+" = \\new Lyrics \\lyricmode {");
	unindent("}");
    }
}
