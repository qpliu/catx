import java.io.*;
import java.util.*;

final class LyricsFileMaker extends FileMaker{
    LyricsFileMaker(Main main)throws IOException{
	super(main,"lyrics");
    }
    void make()throws IOException{
	indent("vocalsLyrics = \\new Lyrics \\lyricmode {");
	unindent("}");
    }
}
