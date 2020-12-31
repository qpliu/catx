import java.io.*;
import java.util.*;

final class ChordsFileMaker extends ChoppedTrackFileMaker{
    ChordsFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.name+"_chords","",arg.name+"Chords",Gpfile.ChordEvent.class,MeasureMaker.SKIP);
    }
    @Override void make()throws IOException{
	indent(lyname+" = \\new ChordNames {");
	makeMeasures();
	unindent("}");
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	return new MeasureMaker.GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		Gpfile.ChordEvent ce=(Gpfile.ChordEvent)list.get(0);
		return is_lhs&&!ce.tie_rhs?ce.chord.ly:"s";
	    }
	};
    }
}
