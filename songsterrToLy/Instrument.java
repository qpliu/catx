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
    abstract Note getNote(Json note);
    String notesToString(PriorityQueue<Event>q){
	return new NotesToString(state).notesToString(q,"r","<",">"," ");
    }
    Rational getDuration(Json j){
	return new Rational(j.get(0).intValue()*state.time_d,j.get(1).intValue());
    }
    PriorityQueue<Event>getEvents(){
	PriorityQueue<Event>q=new PriorityQueue<Event>();
	while (events.size()!=0){
	    Event e=events.peek();
	    if (e.time.compareTo(state.measureStartTime)<0 || e.ghost&&state.argv_no_ghost_notes){
		events.poll();
		continue;
	    }
	    Rational measureEndTime=state.measureStartTime.add(new Rational(state.time_n));
	    if (e.time.compareTo(measureEndTime)>=0)
		break;
	    events.poll();
	    if (e.time.add(e.duration).compareTo(measureEndTime)>0){
		events.add(new Event(e,measureEndTime,e.time.add(e.duration).subtract(measureEndTime)));
		e.duration = measureEndTime.subtract(e.time);
		e.tieLhs = true;
	    }
	    q.add(e);
	}
	return q;
    }
    @Override void engrave(Json measure){
	state.startRelative(this);
	if (state.pass==0){
	    for (Json voice:measure.get("voices").list){
		Rational time=Rational.ZERO;
		for (Json beat:voice.get("beats").list){
		    Rational duration=getDuration(beat.get("duration"));
		    for (Json note:beat.get("notes").list){
			Event e=new Event(state.measureStartTime.add(time).add(state.argv_shift),duration,note,getNote(note));
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
