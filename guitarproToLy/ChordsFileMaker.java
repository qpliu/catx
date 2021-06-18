import java.io.*;
import java.util.*;

final class ChordsFileMaker extends ChoppedTrackFileMaker{
    ChordsFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.name+"_chords","",arg.name+"Chords");
    }
    @Override boolean filterEvents(Gpfile.Event event){
	return event instanceof Gpfile.ChordEvent;
    }
    @Override String getStuff(){
	return super.getStuff()+"\\new ChordNames ";
    }
    @Override MeasureMaker.GetWhatSuffix getRestGetWhatSuffix(){
	return MeasureMaker.SKIP;
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
