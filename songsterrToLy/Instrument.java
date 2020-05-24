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
    private int eventIdCounter;
    class Event implements Comparable<Event>{
	final Rational time;
	Rational duration;
	final int id;
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
	    this.id = eventIdCounter++;
	    Json j;
	    tieRhs = (j=json.get("tie"))!=null && j.booleanValue();
	    ghost = (j=json.get("ghost"))!=null && j.booleanValue();
	    dead = (j=json.get("dead"))!=null && j.booleanValue();
	    note = getNote(json);
	    rest = (j=json.get("rest"))!=null && j.booleanValue() || note==null;
	}
	String tieString(){
	    if (note!=null)
		return note.tieString();
	    return String.valueOf(id);
	}
	@Override public int compareTo(Event e){
	    int i;
	    if ((i=time.compareTo(e.time))!=0)
		return i;
	    if (note==null || e.note==null)
		return (note==null?0:1)-(e.note==null?0:1);
	    return note.compareTo(e.note);
	}
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder();
	    if (rest)
		sb.append('r');
	    else{
		if (ghost)
		    sb.append("\\parenthesize ");
		if (dead)
		    sb.append("\\deadNote ");
		sb.append(note.getLyNote());
	    }
	    return sb.toString();
	}
	String getLySuffix(){
	    StringBuilder sb=new StringBuilder();
	    if (!rest){
		sb.append(note.getLySuffix());
		if (tieLhs)
		    sb.append('~');
	    }
	    return sb.toString();
	}
    }
    abstract Note getNote(Json note);
    String appendTime(String what,String suffix,Rational time){
	if (state.argv_lyrics)
	    suffix = suffix.replace("~","");
	if (time.signum()<0)
	    throw new RuntimeException();
	StringBuilder sb=new StringBuilder();
	BigInteger tuplet_d=time.d;
	while (!tuplet_d.testBit(0))
	    tuplet_d = tuplet_d.shiftRight(1);
	if (!tuplet_d.equals(BigInteger.ONE)){
	    Rational tuplet=new Rational(tuplet_d,BigInteger.ONE);
	    while (tuplet.compareTo(Rational.TWO)>0)
		tuplet = tuplet.divide(2);
	    sb.append("\\tuplet "+tuplet+" { ");
	    sb.append(appendTime(what,suffix,time.multiply(tuplet)));
	    sb.append(" }");
	}else if (time.signum()!=0){
	    Rational d=new Rational(state.time_d);
	    BigInteger n=BigInteger.ONE;
	    while (d.compareTo(time)>0){
		n = n.shiftLeft(1);
		d = d.divide(2);
	    }
	    sb.append(what);
	    sb.append(n);
	    time = time.subtract(d);
	    if (time.compareTo(d.divide(2))>=0){
		sb.append('.');
		time = time.subtract(d.divide(2));
	    }
	    sb.append(suffix);
	    if (time.signum()!=0){
		if (state.argv_lyrics)
		    what = "\\skip";
		if (!what.equals("r") && !what.equals("\\skip") && suffix.indexOf('~')==-1)
		    sb.append('~');
		sb.append(' ').append(appendTime(what,suffix,time));
	    }
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
		sb.append(appendTime(rest,"",e.time.subtract(time))).append(' ');
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
	    time = e.time.add(duration);
	}
	if (!time.equals(measureEndTime))
	    sb.append(appendTime(rest,"",measureEndTime.subtract(time))).append(' ');
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
	    if (e.time.compareTo(state.measureStartTime)<0){
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
		    for (Json note:beat.get("notes").list)
			events.add(new Event(state.measureStartTime.add(time).add(state.argv_shift),duration,note));
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
	    map.put(e.tieString()+','+e.time.add(e.duration),e);
	for (Iterator<Event>i=events.iterator(); i.hasNext();){
	    Event e=i.next();
	    if (e.tieRhs){
		Event lhs=map.remove(e.tieString()+','+e.time);
		if (lhs==null)
		    System.err.println("Strange tie "+e.tieString());
		else{
		    lhs.duration = lhs.duration.add(e.duration);
		    map.put(lhs.tieString()+','+lhs.time.add(lhs.duration),lhs);
		    i.remove();
		}
	    }
	}
    }
}
