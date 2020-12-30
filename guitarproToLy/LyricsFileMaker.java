import java.io.*;
import java.util.*;

final class LyricsFileMaker extends TrackFileMaker{
    final boolean karaoke;
    LyricsFileMaker(Main main,Arg arg,boolean karaoke)throws IOException{
	super(main,arg,arg.partName,"",arg.partName);
	this.karaoke = karaoke;
    }
    @Override void make()throws IOException{
	indent(lyname+" = \\new Lyrics \\lyricmode {");
	makeMeasures();
	unindent("}");
    }
    @Override void makeMeasure(Gpfile.Measure measure,PriorityQueue<Gpfile.Event>events)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	while (events.size()!=0){
	    Gpfile.Event e=events.poll();
	    if (!e.tie_rhs && e instanceof Gpfile.TextEvent){
		Gpfile.TextEvent te=(Gpfile.TextEvent)e;
		if (te.text!=null){
		    mm.make(te.time,"\"\"",false);
		    mm.make(te.time.add(te.duration),new MeasureMaker.GetWhatSuffix(){
			@Override public String getWhat(boolean is_lhs,boolean is_rhs){
			    return is_lhs?Stuff.quote(te.text):"\\skip";
			}
			@Override public String getSuffix(boolean is_lhs,boolean is_rhs){
			    return "";
			}
		    },true);
		}
	    }
	}
	mm.make(measure.time.add(measure.time_n),"\"\"",false);
	print(mm.tail());
    }
}
