import java.io.*;
import java.util.*;

final class DotextFileMaker extends TrackFileMaker{
    DotextFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName+"text","",arg.partName+"Text");
    }
    @Override void make()throws IOException{
	print(lyname+" =");
	indent("{");
	makeMeasures();
	unindent("}");
    }
    @Override void midi(MusicFileMaker mfm)throws IOException{
	if (arg.midi_who!=null)
	    mfm.print("\\tag #'("+arg.midi_who+") \\new "+getStaffType()+" = "+Stuff.quote(filename)+" \\midi"+lyname);
    }
    @Override void printInclude(FileMaker fm){
	fm.print("\\include "+Stuff.quote(filename+"_midi"));
	fm.print("\\include "+Stuff.quote(filename+"_lyrics"));
    }
    @Override void makeMeasure(Gpfile.Measure measure,PriorityQueue<Gpfile.Event>events)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	while (events.size()!=0){
	    Gpfile.Event e=events.poll();
	    if (!(e instanceof Gpfile.DotextEvent))
		continue;
	    Gpfile.DotextEvent de=(Gpfile.DotextEvent)e;
	    if (de.time.compareTo(mm.time)<0){
		Log.info("Overlapping dotexts.  Ignoring %s",de.dotext);
		continue;
	    }
	    mm.skip(de.time);
	    mm.make(de.time.add(de.duration),new MeasureMaker.GetWhatSuffix(){
		@Override public String getWhat(boolean is_lhs,boolean is_rhs){
		    return '`'+de.dotext+'`';
		}
		@Override public String getSuffix(boolean is_lhs,boolean is_rhs){
		    return is_rhs?"":"~";
		}
	    },false);
	}
	mm.skip(measure.time.add(measure.time_n));
	print(mm.tail());
    }
}
