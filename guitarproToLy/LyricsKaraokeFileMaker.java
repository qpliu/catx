import java.io.*;
import java.util.*;

class LyricsKaraokeFileMaker extends ChoppedTrackFileMaker{
    final Set<String>which_lyrics=new HashSet<String>();
    LyricsKaraokeFileMaker(Main main,Arg arg,String suffix1,String suffix2)throws IOException{
	super(main,arg,arg.name+suffix1,"",arg.name+suffix2);
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
	boolean got_lyric=false;
	for (Gpfile.Event e:list){
	    Gpfile.LyricEvent le=(Gpfile.LyricEvent)e;
	    String l=le.lyric;
	    if (!l.startsWith("!"))
		got_lyric = true;
	    if (!le.tie_rhs){
		if (this instanceof KaraokeFileMaker){
		    if (le.hyphen_rhs)
			l = "-"+l;
		}else if (this instanceof LyricsFileMaker){
		    if (le.hyphen_lhs)
			suffix = " --";
		}
		lyric = lyric==null?l:lyric+'|'+l;
	    }
	}
	if (lyric==null)
	    return got_lyric?MeasureMaker.SKIP:getRestGetWhatSuffix();
	if (!got_lyric){
	    String s=getRest();
	    if (s.length()!=0)
		lyric += '|'+s;
	}
	String lyri=Stuff.quote(lyric);
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
    String getRest(){
	return "";
    }
    @Override final MeasureMaker.GetWhatSuffix getRestGetWhatSuffix(){
	return new MeasureMaker.GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		String s=getRest();
		if (s.length()==0 || !is_lhs)
		    return "\\skip";
		return Stuff.quote(s);
	    }
	};
    }
}
