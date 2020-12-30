import java.io.*;
import java.util.*;

final class Main{
    final Gpfile gpfile;
    final Arg globalarg;
    final List<Arg>trackarg;
    final MusicFileMaker musicFileMaker;
    final MarkupFileMaker markupFileMaker;
    final TrackFileMaker[]trackFileMaker;
    Main(Gpfile gpfile,Arg globalarg,List<Arg>trackarg)throws IOException{
	this.gpfile = gpfile;
	this.globalarg = globalarg;
	this.trackarg = trackarg;
	new Twiddler(globalarg,gpfile).twiddle();
	musicFileMaker = new MusicFileMaker(this);
	markupFileMaker = new MarkupFileMaker(this);
	trackFileMaker = new TrackFileMaker[trackarg.size()];
	for (int i=0; i<trackarg.size(); i++)
	    trackFileMaker[i] = TrackFileMaker.newTrackFileMaker(this,trackarg.get(i));
    }
    void make()throws IOException{
	musicFileMaker.make();
	markupFileMaker.make();
	for (TrackFileMaker tfm:trackFileMaker)
	    if (tfm!=null)
		tfm.make();
    }
}
