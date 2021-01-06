import java.io.*;

final class LyricsFileMaker extends LyricsKaraokeFileMaker{
    private static final MeasureMaker.GetWhatSuffix EMPTYSTRING_GWS=new MeasureMaker.GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return "\\skip";
	}
    };
    LyricsFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,"_lyrics","Lyrics",EMPTYSTRING_GWS);
    }
    @Override void midi(MusicFileMaker mfm)throws IOException{
    }
}
