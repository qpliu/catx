import java.io.*;
import java.util.*;

final class DrumTrackFileMaker extends TrackFileMaker{
    DrumTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName,"colors",arg.partName);
    }
    @Override void make()throws IOException{
	indent(lyname+" = \\new DrumVoice = \"drsb\" \\new DrumVoice = \"drsa\" \\drummode {");
	print("\\context DrumVoice = \"drsa\" \\voiceOne");
	print("\\context DrumVoice = \"drsb\" \\voiceTwo");
	makeMeasures();
	unindent("}");
    }
    @Override void makeMeasure(Gpfile.Measure measure,PriorityQueue<Gpfile.Event>events)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	mm.rest(measure.time.add(measure.time_n));
	print(mm.tail());
    }
}
