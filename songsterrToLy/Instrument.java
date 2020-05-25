import java.math.*;
import java.util.*;

abstract class Instrument extends Engraver{
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
    class Event implements Comparable<Event>{
	final Rational time;
	Rational duration;
	final Json json;
	final Note note;
	final boolean ghost;
	final boolean dead;
	final boolean tieRhs;
	boolean tieLhs;
	final boolean rest;
	Event(Rational time,Rational duration,Json json){
	    this.time = time;
	    this.duration = duration;
	    this.json = json;
	    Json j;
	    tieRhs = (j=json.get("tie"))!=null && j.booleanValue();
	    ghost = (j=json.get("ghost"))!=null && j.booleanValue();
	    dead = (j=json.get("dead"))!=null && j.booleanValue();
	    rest = (j=json.get("rest"))!=null && j.booleanValue();
	    note = getNote(json);
	}
	@Override public int compareTo(Event e){
	    int i;
	    if ((i=time.compareTo(e.time))!=0)
		return i;
	    if ((i=note.compareTo(e.note))!=0)
		return i;
	    if ((i=duration.compareTo(e.duration))!=0)
		return i;
	    return (tieLhs?4:0)-(e.tieLhs?4:0)+(ghost?2:0)-(e.ghost?2:0)+(dead?1:0)-(e.dead?1:0);
	}
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder();
	    if (ghost)
		sb.append("\\parenthesize ");
	    if (dead)
		sb.append("\\deadNote ");
	    sb.append(note.getLyNote());
	    return sb.toString();
	}
    }
    abstract Note getNote(Json note);
    String appendDuration(String what,String suffix,Rational duration,boolean tieLhs){
	if (duration.signum()<0)
	    throw new RuntimeException();
	StringBuilder sb=new StringBuilder();
	BigInteger tuplet_d=duration.d;
	while (!tuplet_d.testBit(0))
	    tuplet_d = tuplet_d.shiftRight(1);
	if (!tuplet_d.equals(BigInteger.ONE)){
	    Rational tuplet=new Rational(tuplet_d,BigInteger.ONE);
	    while (tuplet.compareTo(Rational.TWO)>0)
		tuplet = tuplet.divide(2);
	    sb.append("\\tuplet "+tuplet+" { ");
	    sb.append(appendDuration(what,suffix,duration.multiply(tuplet),tieLhs));
	    sb.append(" }");
	}else if (duration.signum()!=0){
	    Rational d=new Rational(state.time_d);
	    BigInteger n=BigInteger.ONE;
	    while (d.compareTo(duration)>0){
		n = n.shiftLeft(1);
		d = d.divide(2);
	    }
	    sb.append(what);
	    sb.append(n);
	    duration = duration.subtract(d);
	    if (duration.compareTo(d.divide(2))>=0){
		sb.append('.');
		duration = duration.subtract(d.divide(2));
	    }
	    sb.append(suffix);
	    if (duration.signum()!=0){
		if (state.argv_lyrics)
		    what = "\\skip";
		if (!what.equals("r") && !what.equals("\\skip"))
		    sb.append('~');
		sb.append(' ').append(appendDuration(what,suffix,duration,tieLhs));
	    }else if (tieLhs)
		sb.append('~');
	}
	return sb.toString();
    }
    String notesToString(PriorityQueue<Event>q,String rest,String lt,String gt,String between){
	Rational measureEndTime=state.measureStartTime.add(state.time_n);
	StringBuilder sb=new StringBuilder();
	Rational time=state.measureStartTime;
	while (q.size()!=0){
	    Event e=q.poll();
	    if (!e.time.equals(time))
		sb.append(appendDuration(rest,"",e.time.subtract(time),false)).append(' ');
	    List<Event>l=new ArrayList<Event>();
	    Rational duration=e.duration;
	    l.add(e);
	    while (q.size()!=0 && q.peek().time.equals(e.time)){
		if (q.peek().duration.compareTo(duration)<0)
		    duration = q.peek().duration;
		l.add(q.poll());
	    }
	    for (Event f:l)
		if (!f.duration.equals(duration)){
		    q.add(new Event(f.time.add(duration),f.duration.subtract(duration),f.json));
		    f.tieLhs = true;
		}
	    if (l.size()==1)
		sb.append(appendDuration(e.toString(),e.note.getLySuffix(),duration,e.tieLhs));
	    else{
		boolean allTies=true;
		for (Event f:l)
		    allTies &= f.tieLhs;
		StringBuilder sb2=new StringBuilder();
		sb2.append(lt);
		for (int i=0; i<l.size(); i++){
		    if (i!=0)
			sb2.append(between);
		    Event f=l.get(i);
		    sb2.append(f).append(f.note.getLySuffix());
		    if (f.tieLhs && !allTies)
			sb2.append('~');
		}
		sb2.append(gt);
		sb.append(appendDuration(sb2.toString(),"",duration,allTies));
	    }
	    sb.append(' ');
	    time = e.time.add(duration);
	}
	if (!time.equals(measureEndTime))
	    sb.append(appendDuration(rest,"",measureEndTime.subtract(time),false)).append(' ');
	sb.append('|');
	return sb.toString();
    }
    String notesToString(PriorityQueue<Event>q){
	return notesToString(q,"r","<",">"," ");
    }
    Rational getDuration(Json j){
	return new Rational(j.get(0).intValue()*state.time_d,j.get(1).intValue());
    }
    PriorityQueue<Event>getEvents(){
	PriorityQueue<Event>q=new PriorityQueue<Event>();
	while (events.size()!=0){
	    Event e=events.peek();
	    if (e.time.compareTo(state.measureStartTime)<0 || e.ghost&&state.argv_omit_ghost_notes){
		events.poll();
		continue;
	    }
	    Rational measureEndTime=state.measureStartTime.add(new Rational(state.time_n));
	    if (e.time.compareTo(measureEndTime)>=0)
		break;
	    events.poll();
	    if (e.time.add(e.duration).compareTo(measureEndTime)>0){
		events.add(new Event(measureEndTime,e.time.add(e.duration).subtract(measureEndTime),e.json));
		e.duration = measureEndTime.subtract(e.time);
		e.tieLhs = true;
	    }
	    q.add(e);
	}
	return q;
    }
    @Override void engrave(Json measure){
	if (state.pass==0){
	    for (Json voice:measure.get("voices").list){
		Rational time=Rational.ZERO;
		for (Json beat:voice.get("beats").list){
		    Rational duration=getDuration(beat.get("duration"));
		    for (Json note:beat.get("notes").list){
			Event e=new Event(state.measureStartTime.add(time).add(state.argv_shift),duration,note);
			if (!e.rest && e.note!=null)
			    events.add(e);
		    }
		    time = time.add(duration);
		}
		if (time.compareTo(new Rational(state.time_n))!=0)
		    throw new RuntimeException(time+" != "+state.time_n);
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
	    map.put(e.note.tieString()+','+e.time.add(e.duration),e);
	for (Iterator<Event>i=events.iterator(); i.hasNext();){
	    Event e=i.next();
	    if (e.tieRhs){
		Event lhs=map.remove(e.note.tieString()+','+e.time);
		if (lhs==null)
		    System.err.println("Strange tie "+e.note.tieString());
		else{
		    lhs.duration = lhs.duration.add(e.duration);
		    map.put(lhs.note.tieString()+','+lhs.time.add(lhs.duration),lhs);
		    i.remove();
		}
	    }
	}
    }
}
