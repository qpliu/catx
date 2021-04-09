import java.io.*;
import java.util.*;

final class VocaloidFileMaker extends VKFileMaker{
    private static final MeasureMaker.GetWhatSuffix EMPTYSTRING_GWS=new MeasureMaker.GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return Stuff.quote(".");
	}
    };
    VocaloidFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,"_vocaloid","Vocaloid",EMPTYSTRING_GWS);
    }
}
