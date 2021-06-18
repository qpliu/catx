import java.io.*;
import java.util.*;

final class VocaloidFileMaker extends VKFileMaker{
    VocaloidFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,"_vocaloid","Vocaloid");
    }
    @Override String getRest(){
	return ".";
    }
}
