import java.io.*;
import java.util.*;

class LyricsKaraokeFileMaker extends ChoppedTrackFileMaker{
    final Set<String>which_lyrics=new HashSet<String>();
    LyricsKaraokeFileMaker(Main main,Arg arg,String suffix1,String suffix2,MeasureMaker.GetWhatSuffix gws)throws IOException{
	super(main,arg,arg.name+suffix1,"",arg.name+suffix2,gws);
	for (StringTokenizer st=new StringTokenizer(arg.which_lyrics,","); st.hasMoreTokens(); which_lyrics.add(st.nextToken()));
    }
    @Override boolean filterEvents(Gpfile.Event event){
	if (!(event instanceof Gpfile.LyricEvent))
	    return false;
	Gpfile.LyricEvent le=(Gpfile.LyricEvent)event;
	if (le.which!=null && which_lyrics.size()!=0 && !which_lyrics.contains(le.which))
	    return false;
	if (le.lyric==null || le.lyric.length()==0 || le.lyric.equals("_") || le.lyric.equals("+"))
	    return false;
	return true;
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	String lyric=null;
	String suffix="";
	int addMinus=0;
	for (Gpfile.Event e:list)
	    if (!e.tie_rhs){
		Gpfile.LyricEvent le=(Gpfile.LyricEvent)e;
		if (lyric!=null && this instanceof LyricsFileMaker){
		    Log.info("Junking simultaneous lyric %s",le.lyric);
		    continue;
		}
		String l=le.lyric;
		if (this instanceof KaraokeFileMaker){
		    if (l.startsWith("!mark="))
			addMinus++;
		    else
			--addMinus;
		    if (le.hyphen_rhs)
			l = "-"+l;
		}else if (le.hyphen_lhs)
		    suffix = " --";
		lyric = lyric==null?l:lyric+'|'+l;
	    }
	if (addMinus>0)
		lyric += "|-";
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
    @Override String getStuff(){
	return super.getStuff()+"\\new Lyrics \\lyricmode ";
    }
}
