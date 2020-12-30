import java.io.*;
import java.util.*;

class TextFileMaker extends TrackFileMaker{
    TextFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName+"text","","midi"+arg.partName+"text");
    }
}
