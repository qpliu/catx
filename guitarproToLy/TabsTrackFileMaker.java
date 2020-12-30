import java.io.*;
import java.util.*;

final class TabsTrackFileMaker extends TrackFileMaker{
    TabsTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.partName,"tabs",arg.partName);
    }
    @Override void makeMeasure(Gpfile.Measure measure)throws IOException{
    }
}
