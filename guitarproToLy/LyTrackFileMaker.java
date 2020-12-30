import java.io.*;
import java.util.*;

final class LyTrackFileMaker extends TrackFileMaker{
    LyTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName,"",arg.partName);
    }
    @Override void makeMeasure(Gpfile.Measure measure)throws IOException{
    }
}
