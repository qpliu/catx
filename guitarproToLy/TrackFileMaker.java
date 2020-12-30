import java.io.*;
import java.util.*;

abstract class TrackFileMaker extends FileMaker{
    final String lyname;
    Arg arg;
    static TrackFileMaker newTrackFileMaker(Main main,Arg arg)throws IOException{
	if (arg.partName==null)
	    return null;
	if (arg.output_text)
	    return new TextFileMaker(main,arg);
	if (arg.output_chords)
	    return new ChordsFileMaker(main,arg);
	if (arg.output_lyrics)
	    return new LyricsFileMaker(main,arg,false);
	if (arg.output_karaoke)
	    return new LyricsFileMaker(main,arg,true);
	if (main.gpfile.tracks[arg.trackNumber].isDrums)
	    return new DrumTrackFileMaker(main,arg);
	if (arg.output_tabs)
	    return new TabsTrackFileMaker(main,arg);
	return new LyTrackFileMaker(main,arg);
    }
    TrackFileMaker(Main main,Arg arg,String fn,String suffix,String lyname)throws IOException{
	super(main,fn,suffix);
	this.arg = arg;
	this.lyname = lyname;
    }
    void make()throws IOException{
	indent(lyname+" = {");
	makeMeasures();
	unindent("}");
    }
    final void makeMeasures()throws IOException{
	String tripletFeel=null;
	for (Gpfile.Measure measure:main.gpfile.measures){
	    if (measure.rehearsalMark!=null)
		noindent("\\mymark "+Stuff.escape(measure.rehearsalMark)+" #"+measure.name);
	    if (tripletFeel!=null && !tripletFeel.equals(measure.tripletFeel))
		unindent(/*{*/"}");
	    if (measure.tripletFeel!=null && !measure.tripletFeel.equals(tripletFeel))
		indent(measure.tripletFeel+" {"/*}*/);
	    tripletFeel = measure.tripletFeel;
	    makeMeasure(measure);
	}
	if (tripletFeel!=null)
	    unindent(/*{*/"}");
    }
    abstract void makeMeasure(Gpfile.Measure measure)throws IOException;
}
