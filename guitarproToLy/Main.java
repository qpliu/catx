import java.io.*;
import java.util.*;

final class Main{
    final Gpfile gpfile;
    final Arg globalarg;
    final MusicFileMaker musicFileMaker;
    final MarkupFileMaker markupFileMaker;
    final List<TrackFileMaker>trackFileMaker=new ArrayList<TrackFileMaker>();
    Main(Gpfile gpfile,Arg globalarg,List<Arg>trackargs)throws IOException{
	this.gpfile = gpfile;
	this.globalarg = globalarg;
	Twiddler twiddler=new Twiddler(this);
	twiddler.twiddle();
	musicFileMaker = new MusicFileMaker(this);
	markupFileMaker = new MarkupFileMaker(this);
	for (Arg arg:trackargs)
	    TrackFileMaker.addTrackFileMaker(this,arg);
	for (TrackFileMaker tfm:trackFileMaker)
	    twiddler.twiddleTrack(tfm);
    }
    void make()throws IOException{
	musicFileMaker.make();
	markupFileMaker.make();
	for (TrackFileMaker tfm:trackFileMaker)
	    if (tfm!=null)
		tfm.make();
    }
}
