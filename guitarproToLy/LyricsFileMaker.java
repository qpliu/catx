import java.io.*;

final class LyricsFileMaker extends LyricsKaraokeFileMaker{
    LyricsFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,"_lyrics","Lyrics");
    }
    @Override void midi(MusicFileMaker mfm)throws IOException{
    }
}
