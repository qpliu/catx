import java.io.*;
import java.util.*;

final class ChordsFileMaker extends TrackFileMaker{
    ChordsFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName,"",arg.partName);
    }
    void make()throws IOException{
	indent(lyname+" = \\new ChordNames {");
	makeMeasures();
	unindent("}");
    }
    @Override void makeMeasure(Gpfile.Measure measure,PriorityQueue<Gpfile.Event>events)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	while (events.size()!=0){
	    Gpfile.Event e=events.poll();
	    if (!e.tie_rhs && e instanceof Gpfile.ChordEvent){
		Gpfile.ChordEvent ce=(Gpfile.ChordEvent)e;
		if (ce.chord.ly!=null){
		    mm.skip(ce.time);
		    mm.make(ce.time.add(ce.duration),ce.chord.ly,"","",false);
		}
	    }
	}
	mm.skip(measure.time.add(measure.time_n));
	print(mm.tail());
    }
}
