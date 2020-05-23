import java.util.*;

abstract class Instrument extends Engraver{
    static final int DIVISION=384;
    abstract boolean matches(State state);
    final PriorityQueue<Event>events=new PriorityQueue<Event>();
    void printHead(){
	indent(state.argv_partName+" = {");
    }
    void printFoot(){
	unindent("}");
    }
    interface Note extends Comparable<Note>{
	String getLyNote();
	default String getLySuffix(){
	    return "";
	}
	default String tieString(){
	    return getLyNote()+getLySuffix();
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
    private int eventIdCounter;
    class Event implements Comparable<Event>{
	final int time;
	int duration;
	final int id;
	final Json json;
	final Note note;
	final boolean ghost;
	final boolean tieRhs;
	boolean tieLhs;
	final boolean rest;
	Event(int time,int duration,Json json){
	    this.time = time;
	    this.duration = duration;
	    this.json = json;
	    this.id = eventIdCounter++;
	    Json j;
	    tieRhs = (j=json.get("tie"))!=null && j.booleanValue();
	    ghost = (j=json.get("ghost"))!=null && j.booleanValue();
	    rest = (j=json.get("rest"))!=null && j.booleanValue();
	    note = getNote(json);
	}
	String tieString(){
	    if (note!=null)
		return note.tieString();
	    return String.valueOf(id);
	}
	@Override public int compareTo(Event e){
	    if (time!=e.time)
		return Integer.compare(time,e.time);
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
	    if (tieLhs)
		sb.append('~');
	    return sb.toString();
	}
    }
    abstract Note getNote(Json note);
    String appendTime(String what,String suffix,int time){
	if (time<0)
	    throw new RuntimeException();
	StringBuilder sb=new StringBuilder();
	if (time%3!=0){
	    sb.append("\\tuplet 3/2 { ");
	    sb.append(appendTime(what,suffix,time*3/2));
	    sb.append(" }");
	}else if (time!=0){
	    int d=DIVISION*state.time_d;
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
		sb.append(' ').append(appendTime(what,suffix,time));
	    }
	}
	return sb.toString();
    }
    String notesToString(PriorityQueue<Event>q,String rest,String lt,String gt,String between){
	int measureEndTime=state.measureStartTime+state.time_n*DIVISION;
	StringBuilder sb=new StringBuilder();
	int time=state.measureStartTime;
	while (q.size()!=0){
	    Event e=q.poll();
	    if (e.time!=time)
		sb.append(appendTime(rest,"",e.time-time)).append(' ');
	    List<Event>l=new ArrayList<Event>();
	    int duration=e.duration;
	    l.add(e);
	    while (q.size()!=0 && q.peek().time==e.time){
		duration = Math.min(q.peek().duration,duration);
		l.add(q.poll());
	    }
	    for (Event f:l)
		if (f.duration!=duration){
		    q.add(new Event(f.time+duration,f.duration-duration,f.json));
		    f.tieLhs = true;
		}
	    if (l.size()==1)
		sb.append(appendTime(e.toString(),e.getLySuffix(),duration));
	    else{
		StringBuilder sb2=new StringBuilder();
		sb2.append(lt);
		for (int i=0; i<l.size(); i++){
		    if (i!=0)
			sb2.append(between);
		    sb2.append(l.get(i)).append(l.get(i).getLySuffix());
		}
		sb2.append(gt);
		sb.append(appendTime(sb2.toString(),"",duration));
	    }
	    sb.append(' ');
	    time = e.time+duration;
	}
	if (time!=measureEndTime)
	    sb.append(appendTime(rest,"",measureEndTime-time)).append(' ');
	sb.append('|');
	return sb.toString();
    }
    String notesToString(PriorityQueue<Event>q){
	return notesToString(q,"r","<",">"," ");
    }
    int getDuration(Json j){
	int duration_n=j.get(0).intValue();
	int duration_d=j.get(1).intValue();
	if (DIVISION*state.time_d*duration_n%duration_d!=0)
	    throw new RuntimeException("Bad duration "+j.unparse());
	return DIVISION*state.time_d*duration_n/duration_d;
    }
    PriorityQueue<Event>getEvents(){
	PriorityQueue<Event>q=new PriorityQueue<Event>();
	while (events.size()!=0){
	    Event e=events.peek();
	    if (e.time<state.measureStartTime){
		events.poll();
		continue;
	    }
	    int measureEndTime=state.measureStartTime+state.time_n*DIVISION;
	    if (e.time>=measureEndTime)
		break;
	    events.poll();
	    if (e.time+e.duration>measureEndTime){
		events.add(new Event(measureEndTime,e.time+e.duration-measureEndTime,e.json));
		e.duration = measureEndTime-e.time;
		e.tieLhs = true;
	    }
	    q.add(e);
	}
	return q;
    }
    @Override void engrave(Json measure){
	if (state.pass==0){
	    for (Json voice:measure.get("voices").list){
		int time=0;
		for (Json beat:voice.get("beats").list){
		    int duration=getDuration(beat.get("duration"));
		    for (Json note:beat.get("notes").list)
			events.add(new Event(state.measureStartTime+time+state.argv_shift,duration,note));
		    time += duration;
		}
		if (time!=state.time_n*DIVISION)
		    throw new RuntimeException(time+" != "+state.time_n+"*"+DIVISION);
	    }
	}else
	    print(notesToString(getEvents()));
    }
    @Override void setState(State state){
	super.setState(state);
	if (state.pass==1)
	    joinTies();
    }
    private void joinTies(){
	Map<String,Event>map=new HashMap<String,Event>();
	for (Event e:events)
	    map.put(e.tieString()+','+(e.time+e.duration),e);
	for (Iterator<Event>i=events.iterator(); i.hasNext();){
	    Event e=i.next();
	    if (e.tieRhs){
		Event lhs=map.remove(e.tieString()+','+e.time);
		if (lhs==null)
		    System.err.println("Strange tie "+e.tieString());
		else{
		    lhs.duration += e.duration;
		    map.put(lhs.tieString()+','+(lhs.time+lhs.duration),lhs);
		    i.remove();
		}
	    }
	}
    }
}
