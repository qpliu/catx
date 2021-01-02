import java.io.*;
import java.util.*;

abstract class TrackFileMaker extends FileMaker{
    final Gpfile.Track track;
    final String lyname;
    Arg arg;
    static void addTrackFileMaker(Main main,Arg arg)throws IOException{
	if (arg.output_chords)
	    main.trackFileMaker.add(new ChordsFileMaker(main,arg));
	if (arg.output_dotext)
	    main.trackFileMaker.add(new DotextFileMaker(main,arg));
	if (arg.output_drums)
	    main.trackFileMaker.add(new DrumTrackFileMaker(main,arg));
	if (arg.output_lilypond)
	    main.trackFileMaker.add(new LyTrackFileMaker(main,arg));
	if (arg.output_lyrics)
	    main.trackFileMaker.add(new LyricsFileMaker(main,arg,false));
	if (arg.output_karaoke)
	    main.trackFileMaker.add(new LyricsFileMaker(main,arg,true));
    }
    TrackFileMaker(Main main,Arg arg,String fn,String suffix,String lyname)throws IOException{
	super(main,fn,suffix);
	this.arg = arg;
	this.lyname = lyname;
	track = main.gpfile.tracks[arg.trackNumber];
    }
    void make()throws IOException{
	indent(lyname+" = {");
	makeMeasures();
	unindent("}");
    }
    final String getStaffType(){
	return this instanceof DrumTrackFileMaker?"DrumStaff":"Staff";
    }
    void printMeasure(String measure){
	print(measure);
    }
    final void makeMeasures()throws IOException{
	for (String s:arg.music_extra)
	    print(s);
	Collections.sort(track.events);
	String tripletFeel=null;
	PriorityQueue<Gpfile.Event>q=new PriorityQueue<Gpfile.Event>(track.events);
	for (Gpfile.Measure measure:main.gpfile.measures){
	    printMeasureStuff(measure);
	    if (tripletFeel!=null && !tripletFeel.equals(measure.tripletFeel))
		unindent(/*{*/"}");
	    if (measure.tripletFeel!=null && !measure.tripletFeel.equals(tripletFeel))
		indent(measure.tripletFeel+" {"/*}*/);
	    tripletFeel = measure.tripletFeel;
	    List<Gpfile.Event>events=new ArrayList<Gpfile.Event>();
	    while (q.size()!=0){
		Gpfile.Event e=q.poll();
		if (e.time.compareTo(measure.time)<0){
		    Gpfile.Event[]cut=Stuff.cutEvent(e,measure.time);
		    if (cut[1]==null)
			continue;
		    e = cut[1];
		    e.tie_rhs = false;
		}
		Gpfile.Event[]cut=Stuff.cutEvent(e,measure.time.add(measure.time_n));
		if (cut[1]!=null)
		    q.add(cut[1]);
		if (cut[0]==null)
		    break;
		events.add(cut[0]);
	    }
	    printMeasure(makeMeasure(measure,events));
	    printMeasureStuffEnd(measure);
	}
	if (tripletFeel!=null)
	    unindent(/*{*/"}");
    }
    abstract String makeMeasure(Gpfile.Measure measure,List<Gpfile.Event>events)throws IOException;
    void layout(MusicFileMaker mfm)throws IOException{
	if (arg.layout_who!=null)
	    mfm.print("\\tag #'("+arg.layout_who+") \\"+lyname);
    }
    void midi(MusicFileMaker mfm)throws IOException{
	if (arg.midi_who!=null)
	    mfm.print("\\tag #'("+arg.midi_who+") \\"+lyname);
    }
}
