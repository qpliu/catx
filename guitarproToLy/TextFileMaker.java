import java.io.*;
import java.util.*;

class TextFileMaker extends TrackFileMaker{
    static TextFileMaker newTextFileMaker(Main main,int index)throws IOException{
	Arg arg=main.trackarg[index];
	if (!arg.output_text)
	    return null;
	return new TextFileMaker(main,arg);
    }
    TextFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName+"text","","midi"+arg.partName+"text");
    }
    void make()throws IOException{
	indent(lyname+" = {");
	makeMeasures();
	unindent("}");
    }
    void makeMeasure(Gpfile.Measure measure)throws IOException{
    }
}
