import java.util.*;

abstract class Instrument extends Engraver{
    static final int DIVISION=384;
    abstract boolean matches(State state);
    abstract void printHead();
    void printFoot(){
	/*{*/ unindent("}");
    }
    interface Note extends Comparable<Note>{
	String getLyNote();
	default String getLySuffix(){
	    return "";
	}
    }
    static class MidiNote implements Note{
	private final int note;
	MidiNote(int note){
	    this.note = note;
	}
	public int compareTo(Note n){
	    return Integer.compare(note,((MidiNote)n).note);
	}
	@Override public String getLyNote(){
	    return Stuff.midi2ly(note);
	}
    }
    class Event implements Comparable<Event>{
	final int time;
	final int duration;
	final Note note;
	final boolean ghost;
	final boolean tie;
	boolean tie_from;
	final boolean rest;
	Event(int time,int duration,Json note){
	    this.time = time;
	    this.duration = duration;
	    Json j;
	    tie = (j=note.get("tie"))!=null && j.booleanValue();
	    ghost = (j=note.get("ghost"))!=null && j.booleanValue();
	    rest = (j=note.get("rest"))!=null && j.booleanValue();
	    this.note = getNote(note);
	}
	@Override public int compareTo(Event e){
	    if (time!=e.time)
		return Integer.compare(time,e.time);
	    if (duration!=e.duration)
		return Integer.compare(duration,e.duration);
	    if (note==null || e.note==null)
		return (note==null?0:1)-(e.note==null?0:1);
	    return note.compareTo(e.note);
	}
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder();
	    if (ghost)
		sb.append("\\parenthesize ");
	    if (rest)
		sb.append('r');
	    else
		sb.append(note.getLyNote());
	    return sb.toString();
	}
	String getLySuffix(){
	    StringBuilder sb=new StringBuilder();
	    if (note!=null)
		sb.append(note.getLySuffix());
	    if (tie_from)
		sb.append('~');
	    return sb.toString();
	}
    }
    abstract Note getNote(Json note);
    String appendTime(String what,String suffix,int time){
	StringBuilder sb=new StringBuilder();
	if (time%3!=0){
	    sb.append("\\tuplet 3/2 { ");
	    sb.append(appendTime(what,suffix,time*3/2));
	    sb.append(" }");
	}else if (time!=0){
	    int d=DIVISION*state.timed;
	    int n=1;
	    while (d>time){
		n <<= 1;
		d >>= 1;
	    }
	    sb.append(what);
	    sb.append(n);
	    time -= d;
	    if (time>=d/2){
		sb.append('.');
		time -= d/2;
	    }
	    sb.append(suffix);
	    if (time!=0){
		if (!what.equals("r") && suffix.indexOf('~')==-1)
		    sb.append('~');
		sb.append(appendTime(what,suffix,time));
	    }
	}
	return sb.toString();
    }
    String notesToString(List<Event>list){
	StringBuilder sb=new StringBuilder();
	int time=0;
	for (int i=0; i<list.size();){
	    int start=list.get(i).time;
	    if (start!=time)
		sb.append(appendTime("r","",start-time)).append(' ');
	    int j=i;
	    int duration=list.get(i).duration;
	    while (++i<list.size()&&list.get(i).time==start)
		if (list.get(i).duration!=duration)
		    throw new RuntimeException();
	    if (i>j+1){
		StringBuilder sb2=new StringBuilder();
		sb2.append('<');
		for (int k=j; k<i; k++){
		    if (k!=j)
			sb2.append(' ');
		    Event e=list.get(k);
		    sb2.append(e);
		    sb2.append(e.getLySuffix());
		}
		sb2.append('>');
		sb.append(appendTime(sb2.toString(),"",duration));
	    }else{
		Event e=list.get(j);
		sb.append(appendTime(e.toString(),e.getLySuffix(),duration));
	    }
	    sb.append(' ');
	    time = start+duration;
	}
	if (time!=state.timen*DIVISION)
	    sb.append(appendTime("r","",state.timen*DIVISION-time)).append(' ');
	sb.append('|');
	return sb.toString();
    }
    int getDuration(Json j){
	int duration_n=j.get(0).intValue();
	int duration_d=j.get(1).intValue();
	if (DIVISION*state.timed*duration_n%duration_d!=0)
	    throw new RuntimeException("Bad duration "+j.unparse());
	return DIVISION*state.timed*duration_n/duration_d;
    }
    private List<Event>getEvents(Json measure,int start){
	List<Event>list=new ArrayList<Event>();
	for (Json voice:measure.get("voices").list){
	    int time=0;
	    for (Json beat:voice.get("beats").list){
		int duration=getDuration(beat.get("duration"));
		for (Json note:beat.get("notes").list)
		    list.add(new Event(start+time,duration,note));
		time += duration;
	    }
	    if (start==0 && time!=state.timen*DIVISION)
		throw new RuntimeException(time+" != "+state.timen+"*"+DIVISION);
	}
	return list;
    }
    private void makeTies(List<Event>from,List<Event>to){
	for (Event t:to)
	    if (t.tie)
		for (Event f:from)
		    if (f.time+f.duration==t.time && f.note.compareTo(t.note)==0)
			f.tie_from = true;
    }
    @Override void engrave(Json measure,Json nextMeasure){
	List<Event>list=getEvents(measure,0);
	makeTies(list,list);
	if (nextMeasure!=null)
	    makeTies(list,getEvents(nextMeasure,DIVISION*state.timen));
	Collections.sort(list);
	print(notesToString(list));
    }
}
