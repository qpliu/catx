import java.math.*;
import java.util.*;

abstract class Instrument extends Engraver{
    abstract boolean matches(State state);
    final PriorityQueue<Event>events=new PriorityQueue<Event>();
    void printHead(){
	print(state.argv_partName+" =");
	indent("{");
    }
    void printTwo(){
	noindent("% THIS FILE IS GENERATED.  DO NOT EDIT.  If you really really want to change stuff in this file and you can't think of any other way to do what you want, get rid of this comment and remove line from \"get\" script that generated this.");
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
	if (state.pass==0){
	    for (Json voice:measure.get("voices").list){
		Rational time=Rational.ZERO;
		for (Json beat:voice.get("beats").list){
		    Rational duration=getDuration(beat.get("duration"));
		    List<Event>l=new ArrayList<Event>();
		    int lowest=Integer.MAX_VALUE;
		    int highest=Integer.MIN_VALUE;
		    for (Json note:beat.get("notes").list){
			Event e=new Event(state,state.measureStartTime.add(time).add(state.argv_shift),duration,note,getNote(note));
			if (!e.rest && e.note!=null){
			    l.add(e);
			    if (e.note instanceof MidiNote){
				lowest = Math.min(lowest,((MidiNote)e.note).note);
				highest = Math.max(highest,((MidiNote)e.note).note);
			    }
			}
		    }
		    for (Event e:l)
			if (!(e.note instanceof MidiNote) || !(state.argv_only_highest_note && ((MidiNote)e.note).note<highest) && !(state.argv_only_lowest_note && ((MidiNote)e.note).note>lowest))
			    events.add(e);
		    time = time.add(duration);
		}
		if (time.compareTo(new Rational(state.time_n))!=0)
		    throw new RuntimeException("sum_of_beats="+time+" time_signature="+state.time_n+" measureStarttime="+state.measureStartTime);
	    }
	}else
	    print(notesToString(getEvents()));
    }
    @Override void setState(State state){
	super.setState(state);
	if (state.pass==1){
	    joinTies();
	    if (state.argv_output_text)
		TextEventMaker.makeTextEvents(state,events);
	}
    }
    private void joinTies(){
	Map<String,List<Event>>map=new HashMap<String,List<Event>>();
	while (events.size()!=0){
	    Event e=events.poll();
	    if (e.tieLhs){
		List<Event>l=map.get(e.note.tieString()+','+e.time);
		if (l==null || l.size()==0)
		    System.err.println("Strange tie "+e.note.tieString()+" "+state.argv_partName+" time="+e.time+" beats");
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
