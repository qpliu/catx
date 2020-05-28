import java.math.*;
import java.util.*;

abstract class Instrument extends Engraver{
    abstract boolean matches(State state);
    final PriorityQueue<Event>events=new PriorityQueue<Event>();
    void printHead(){
	print(state.argv_partName+" =");
	indent("{");
    }
    void printFoot(){
	unindent("}");
    }
    abstract Note getNote(Json note);
    String notesToString(PriorityQueue<Event>q){
	return NotesToString.get(state).notesToString(q);
    }
    Rational getDuration(Json j){
	return new Rational(j.get(0).intValue()*state.time_d,j.get(1).intValue());
    }
    PriorityQueue<Event>getEvents(){
	PriorityQueue<Event>q=new PriorityQueue<Event>();
	while (events.size()!=0){
	    Event e=events.peek();
	    if (e.isJunk() || e.time.compareTo(state.measureStartTime)<0){
		events.poll();
		continue;
	    }
	    Rational measureEndTime=state.measureStartTime.add(new Rational(state.time_n));
	    if (e.time.compareTo(measureEndTime)>=0)
		break;
	    events.poll();
	    Event[]lr=e.split(measureEndTime.subtract(e.time));
	    if (lr[1]!=null)
		events.add(lr[1]);
	    q.add(lr[0]);
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
			Event e=new Event(state,state.measureStartTime.add(time).add(state.argv_shift),duration,note,getNote(note));
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
	Map<String,List<Event>>map=new HashMap<String,List<Event>>();
	while (events.size()!=0){
	    Event e=events.poll();
	    if (e.tieLhs){
		List<Event>l=map.get(e.note.tieString()+','+e.time);
		if (l==null || l.size()==0)
		    System.err.println("Strange tie "+e.note.tieString());
		else{
		    events.add(l.remove(l.size()-1).tie(e));
		    continue;
		}
	    }
	    map.computeIfAbsent(e.tieString(),x->new ArrayList<Event>()).add(e);
	}
	for (List<Event>l:map.values())
	    for (Event e:l)
		events.add(e);
    }
}
