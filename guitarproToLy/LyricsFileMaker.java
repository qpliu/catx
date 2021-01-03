import java.io.*;
import java.util.*;

final class LyricsFileMaker extends ChoppedTrackFileMaker{
    private static final MeasureMaker.GetWhatSuffix EMPTYSTRING_GWS=new MeasureMaker.GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return "\"\"";
	}
    };
    private static final MeasureMaker.GetWhatSuffix HYPHENSTRING_GWS=new MeasureMaker.GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return is_lhs?"\"-\"":"\"\"";
	}
    };
    final boolean karaoke;
    final Set<String>which_lyrics=new HashSet<String>();
    LyricsFileMaker(Main main,Arg arg,boolean karaoke)throws IOException{
	super(main,arg,arg.name+(karaoke?"_karaoke":"_lyrics"),"",arg.name+(karaoke?"Karaoke":"Lyrics"),karaoke?HYPHENSTRING_GWS:EMPTYSTRING_GWS);
	this.karaoke = karaoke;
	for (StringTokenizer st=new StringTokenizer(arg.which_lyrics,","); st.hasMoreTokens(); which_lyrics.add(st.nextToken()));
	if (arg.generate_lyrics)
	    generate_lyrics();
    }
    @Override boolean filterEvents(Gpfile.Event event){
	return event instanceof Gpfile.LyricEvent && (which_lyrics.size()==0 || which_lyrics.contains(((Gpfile.LyricEvent)event).which));
    }
    private void generate_lyrics(){
	Queue<Gpfile.NoteEvent>queue=new PriorityQueue<Gpfile.NoteEvent>();
	for (Gpfile.Event e:track.events)
	    if (e instanceof Gpfile.NoteEvent)
		queue.add((Gpfile.NoteEvent)e);
	for (Gpfile.NoteEvent ne; (ne=queue.poll())!=null;){
	    while (queue.size()!=0 && queue.peek().time==ne.time)
		ne = queue.poll();
	    track.events.add(new Gpfile.LyricEvent(ne.time,ne.duration,Stuff.midi2ly(ne.getNote(),arg),false,false,"generated"));
	}
    }
    @Override String getStuff(){
	return super.getStuff()+"\\new Lyrics \\lyricmode ";
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	String lyric=null;
	String suffix="";
	for (Gpfile.Event e:list)
	    if (!e.tie_rhs){
		Gpfile.LyricEvent le=(Gpfile.LyricEvent)e;
		if (lyric==null){
		    lyric = le.lyric;
		    if (le.hyphen_lhs && !karaoke)
			suffix = " --";
		    if (le.hyphen_rhs && karaoke)
			lyric = "-"+lyric;
		}else
		    Log.info("Junking simultaneous lyric %s",le.lyric);
	    }
	String lyri=lyric==null?"\\skip":Stuff.quote(lyric);
	String suffi=suffix;
	return new MeasureMaker.GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		return is_lhs?lyri:"\\skip";
	    }
	    @Override public String getSuffix(boolean is_lhs,boolean is_rhs){
		return is_lhs?suffi:"";
	    }
	};
    }
    @Override void printMeasure(String measure){
	if (karaoke && measure.endsWith(" |"))
	    measure = measure.substring(0,measure.length()-2);
	print(measure);
    }
    @Override void layout(MusicFileMaker mfm)throws IOException{
	if (!karaoke)
	    super.layout(mfm);
    }
    @Override void midi(MusicFileMaker mfm)throws IOException{
	if (karaoke)
	    super.midi(mfm);
    }
}
