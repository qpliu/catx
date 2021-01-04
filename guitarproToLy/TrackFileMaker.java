import java.io.*;
import java.util.*;

abstract class TrackFileMaker extends FileMaker{
    final Gpfile.Track track;
    final List<Gpfile.Event>trackEvents=new ArrayList<Gpfile.Event>();
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
	    main.trackFileMaker.add(new LyricsFileMaker(main,arg));
	if (arg.output_karaoke)
	    main.trackFileMaker.add(new KaraokeFileMaker(main,arg));
    }
    TrackFileMaker(Main main,Arg arg,String fn,String suffix,String lyname)throws IOException{
	super(main,fn,suffix);
	this.arg = arg;
	this.lyname = lyname;
	track = main.gpfile.tracks[arg.trackNumber];
	for (Gpfile.Event e:track.events)
	    trackEvents.add(e.clone());
    }
    String getStuff(){
	return "";
    }
    final void make()throws IOException{
	String stuff=getStuff();
	indent(lyname+" = "+stuff+"{");
	makeMeasures();
	StringBuilder sb=new StringBuilder("}");
	for (int i=0; i<stuff.length(); i++)
	    if (stuff.charAt(i)=='{')
		sb.append(" }");
	unindent(sb.toString());
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
	String tripletFeel=null;
	PriorityQueue<Gpfile.Event>q=new PriorityQueue<Gpfile.Event>(trackEvents);
	for (Gpfile.Measure measure:main.gpfile.measures){
	    printMeasureStuff(measure);
	    if (tripletFeel!=null && !tripletFeel.equals(measure.tripletFeel))
		unindent(/*{*/"}");
	    if (measure.tripletFeel!=null && !measure.tripletFeel.equals(tripletFeel))
		indent(measure.tripletFeel+" {"/*}*/);
	    tripletFeel = measure.tripletFeel;
	    List<Gpfile.Event>measureEvents=new ArrayList<Gpfile.Event>();
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
		measureEvents.add(cut[0]);
	    }
	    printMeasure(makeMeasure(measure,measureEvents));
	    printMeasureStuffEnd(measure);
	}
	if (tripletFeel!=null)
	    unindent(/*{*/"}");
    }
    abstract String makeMeasure(Gpfile.Measure measure,List<Gpfile.Event>measureEvents)throws IOException;
    void layout(MusicFileMaker mfm)throws IOException{
	if (arg.layout_who!=null)
	    mfm.print("\\tag #'("+arg.layout_who+") \\"+lyname);
    }
    void midi(MusicFileMaker mfm)throws IOException{
	if (arg.midi_who!=null)
	    mfm.print("\\tag #'("+arg.midi_who+") \\"+lyname);
    }
}
