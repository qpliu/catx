import java.io.*;
import java.util.*;

final class LyricsFileMaker extends ChoppedTrackFileMaker{
    private static final MeasureMaker.GetWhatSuffix EMPTYSTRING_GWS=new MeasureMaker.GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return "\"\"";
	}
    };
    final boolean karaoke;
    LyricsFileMaker(Main main,Arg arg,boolean karaoke)throws IOException{
	super(main,arg,arg.partName,"",arg.partName,Gpfile.LyricEvent.class,EMPTYSTRING_GWS);
	this.karaoke = karaoke;
    }
    @Override void make()throws IOException{
	indent(lyname+" = \\new Lyrics \\lyricmode {");
	makeMeasures();
	unindent("}");
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	String lyric="\\skip";
	for (Gpfile.Event e:list)
	    if (!e.tie_rhs)
		lyric = ((Gpfile.LyricEvent)e).lyric;
	String lyri=Stuff.quote(lyric);
	return new MeasureMaker.GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		return is_rhs?lyri:"\\skip";
	    }
	};
    }
    @Override void printMeasure(String measure){
	if (karaoke && measure.endsWith(" |"))
	    measure = measure.substring(0,measure.length()-2);
	print(measure);
    }
}
