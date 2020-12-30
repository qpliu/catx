import java.io.*;
import java.util.*;

final class DrumTrackFileMaker extends TrackFileMaker{
    DrumTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName,"colors",arg.partName);
    }
    void make()throws IOException{
	indent(lyname+" = \\new DrumVoice = \"drsb\" \\new DrumVoice = \"drsa\" \\drummode {");
	print("\\context DrumVoice = \"drsa\" \\voiceOne");
	print("\\context DrumVoice = \"drsb\" \\voiceTwo");
	makeMeasures();
	unindent("}");
    }
}
