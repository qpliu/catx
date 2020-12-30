import java.io.*;
import java.util.*;

final class ChordsFileMaker extends TrackFileMaker{
    ChordsFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName,"",arg.partName);
    }
    void make()throws IOException{
	indent(lyname+" = \\chordmode {");
	makeMeasures();
	unindent("}");
    }
    void makeMeasure(Gpfile.Measure measure)throws IOException{
    }
}
