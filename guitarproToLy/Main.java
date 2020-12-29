import java.io.*;
import java.util.*;

final class Main{
    final Gpfile gpfile;
    final Arg globalarg;
    final Arg[]trackarg;
    final MusicFileMaker musicFileMaker;
    final MarkupFileMaker markupFileMaker;
    final LyricsFileMaker lyricsFileMaker;
    final KaraokeFileMaker karaokeFileMaker;
    final TrackFileMaker[]trackFileMaker;
    Main(Gpfile gpfile,Arg globalarg,Arg[]trackarg)throws IOException{
	this.gpfile = gpfile;
	this.globalarg = globalarg;
	this.trackarg = trackarg;
	musicFileMaker = new MusicFileMaker(this);
	markupFileMaker = new MarkupFileMaker(this);
	lyricsFileMaker = new LyricsFileMaker(this);
	karaokeFileMaker = new KaraokeFileMaker(this);
	trackFileMaker = new TrackFileMaker[trackarg.length];
	for (int i=0; i<trackarg.length; i++)
	    trackFileMaker[i] = TrackFileMaker.newTrackFileMaker(this,i);
    }
    void make()throws IOException{
	musicFileMaker.make();
	markupFileMaker.make();
	lyricsFileMaker.make();
	karaokeFileMaker.make();
	for (TrackFileMaker tfm:trackFileMaker)
	    if (tfm!=null)
		tfm.make();
    }
}
