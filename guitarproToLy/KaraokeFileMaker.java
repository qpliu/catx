import java.io.*;
import java.util.*;

final class KaraokeFileMaker extends LyricsKaraokeFileMaker{
    private final Rational bounce_time;
    private final int threshold_to_add_count;
    private final int threshold_to_add_bounce;
    private final double short_length;
    private final double long_length;
    private final double maximum_length;
    private static final MeasureMaker.GetWhatSuffix HYPHENSTRING_GWS=new MeasureMaker.GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return is_lhs?"\"-\"":"\"\"";
	}
    };
    KaraokeFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,"_karaoke","Karaoke",HYPHENSTRING_GWS);
	bounce_time = Rational.parseRational(arg.karaoke_lyrics_parameter.getOrDefault("bounce_time","2"));
	threshold_to_add_bounce = Integer.parseInt(arg.karaoke_lyrics_parameter.getOrDefault("threshold_to_add_bounce","4"));
	threshold_to_add_count = Integer.parseInt(arg.karaoke_lyrics_parameter.getOrDefault("threshold_to_add_count","8"));
	short_length = Double.parseDouble(arg.karaoke_lyrics_parameter.getOrDefault("short_length","20"));
	long_length = Double.parseDouble(arg.karaoke_lyrics_parameter.getOrDefault("long_length","40"));
	maximum_length = Double.parseDouble(arg.karaoke_lyrics_parameter.getOrDefault("maximum_length","50"));
	addBounces();
	addLineBreaks();
	addEnd();
    }
    @Override void layout(MusicFileMaker mfm)throws IOException{
    }
    @Override void printMeasure(String measure){
	if (measure.endsWith(" |"))
	    measure = measure.substring(0,measure.length()-2);
	super.printMeasure(measure);
    }
    void addBounces(){
	Rational lastTime=Rational.ZERO;
	for (Gpfile.Event e:getFilteredEvents(trackEvents)){
	    Gpfile.LyricEvent le=(Gpfile.LyricEvent)e;
	    int bounces;
	    for (bounces=0; lastTime.add(bounce_time.multiply(bounces+1)).compareTo(le.time)<=0; bounces++);
	    if (!le.hyphen_rhs && bounces>threshold_to_add_bounce){
		if (lastTime.signum()!=0)
		    --bounces;
		lastTime = le.time.subtract(bounce_time.multiply(bounces));
		for (int i=0; i<bounces; i++){
		    String lyric="@";
		    if (bounces>=threshold_to_add_count && bounces-i<=4){
			lyric = "@|!bg="+(bounces-i);
			if (i==bounces-1)
			    le.lyric += "|!bg=";
		    }
		    trackEvents.add(new Gpfile.LyricEvent(lastTime.add(bounce_time.multiply(i)),bounce_time,lyric,false,false,le.which));
		}
	    }
	    Rational end=le.time.add(le.duration);
	    if (end.compareTo(lastTime)>0)
		lastTime = end;
	}
    }
    double breakScore(Gpfile.LyricEvent previous,Gpfile.LyricEvent next){
	double score=-50;
	char p=previous.lyric.charAt(previous.lyric.length()-1);
	char n=next.lyric.charAt(0);
	if (p=='.')
	    score += 10;
	if (p==',')
	    score += 5;
	if (Character.isUpperCase(n))
	    score += 10;
	score += previous.duration.doubleValue()*4+next.time.subtract(previous.time.add(previous.duration)).doubleValue()*10;
	return score;
    }
    double stringLengthScore(double len){
	if (len<short_length)
	    return len-short_length;
	if (len>long_length)
	    return long_length-len;
	if (len>maximum_length)
	    return Double.NEGATIVE_INFINITY;
	return 0;
    }
    static double stringLength(Gpfile.LyricEvent le){
	String s=le.lyric;
	if (s.startsWith(">"))
	    s = s.substring(1);
	if (s.endsWith("|!bg="))
	    s = s.substring(0,s.length()-5);
	return s.length()+(le.hyphen_rhs?0:1);
    }
    void addLineBreaks(){
	List<Gpfile.LyricEvent>list=new ArrayList<Gpfile.LyricEvent>();
	for (Queue<Gpfile.Event>q=getFilteredEvents(trackEvents); q.size()!=0; list.add((Gpfile.LyricEvent)q.poll()));
	for (int end=0; end<list.size();){
	    Gpfile.LyricEvent le=list.get(end);
	    for (; end<list.size()&&list.get(end).lyric.startsWith("@"); end++);
	    le.lyric = ">"+le.lyric;
	    int start=end;
	    for (; end<list.size()&&!list.get(end).lyric.startsWith("@"); end++);
	    boolean[]lineBreak=new boolean[end-start-1];
	    for (int i=0; i<lineBreak.length; i++)
		lineBreak[i] = !list.get(i).hyphen_lhs;
	    double bestScore=getScore(list,start,lineBreak);
	    for (;;){
		int bestJ=-1;
		for (int j=0; j<lineBreak.length; j++)
		    if (lineBreak[j]){
			lineBreak[j] = false;
			double score=getScore(list,start,lineBreak);
			lineBreak[j] = true;
			if (score>bestScore){
			    bestScore = score;
			    bestJ = j;
			}
		    }
		if (bestJ==-1)
		    break;
		lineBreak[bestJ] = false;
	    }
	    for (int j=0; j<lineBreak.length; j++)
		if (lineBreak[j]){
		    Gpfile.LyricEvent e=list.get(start+j+1);
		    e.lyric = ">"+e.lyric;
		}
	}
    }
    double getScore(List<Gpfile.LyricEvent>list,int start,boolean[]lineBreak){
	double length=0;
	double score=0;
	for (int i=0; i<lineBreak.length; i++){
	    Gpfile.LyricEvent previous=list.get(start+i);
	    Gpfile.LyricEvent next=list.get(start+i+1);
	    length += stringLength(previous);
	    if (lineBreak[i]){
		score += stringLengthScore(length)+breakScore(previous,next);
		length = 0;
	    }
	}
	length += stringLength(list.get(start+lineBreak.length));
	score += stringLengthScore(length);
	return score;
    }
    void addEnd(){
	Gpfile.Measure lastMeasure=main.gpfile.measures[main.gpfile.measures.length-1];
	Rational duration=new Rational(1,8);
	Rational songEnd=lastMeasure.time.add(lastMeasure.time_n).subtract(duration);
	Rational lastTime=Rational.ZERO;
	String which=null;
	for (Gpfile.Event e:getFilteredEvents(trackEvents)){
	    Rational end=e.time.add(e.duration);
	    if (end.compareTo(lastTime)>0)
		lastTime = end;
	    which = ((Gpfile.LyricEvent)e).which;
	}
	lastTime = lastTime.add(bounce_time.multiply(2));
	if (lastTime.compareTo(songEnd)>0)
	    lastTime = songEnd;
	trackEvents.add(new Gpfile.LyricEvent(lastTime,duration,"@@",false,false,which));
    }
}
