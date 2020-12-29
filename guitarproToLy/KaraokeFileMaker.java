import java.io.*;
import java.util.*;

final class KaraokeFileMaker extends FileMaker{
    KaraokeFileMaker(Main main)throws IOException{
	super(main,"karaoke");
    }
    void make()throws IOException{
	indent("karaokeLyrics = \\new Lyrics \\lyricmode {");
	unindent("}");
    }
}
