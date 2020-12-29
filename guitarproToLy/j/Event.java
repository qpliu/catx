import java.math.*;
import java.util.*;

final class Event implements Comparable<Event>{
    final State state;
    final Rational time;
    final Rational duration;
    final Note note;
    final boolean ghost;
    final String slide;
    final boolean dead;
    final boolean tieLhs;
    final boolean tieRhs;
    final boolean hpRhs;
    final boolean rest;
    private Event(Event e,Rational time,Rational duration,boolean tieLhs,boolean tieRhs,boolean hpRhs,Note note){
	state = e.state;
	this.time = time;
	this.duration = duration;
	this.note = note;
	ghost = e.ghost;
	slide = e.slide;
	dead = e.dead;
	this.tieLhs = tieLhs;
	this.tieRhs = tieRhs;
	this.hpRhs = hpRhs;
	rest = e.rest;
    }
    Event(State state,Rational time,Rational duration,Json json,Note note){
	this.state = state;
	this.time = time;
	this.duration = duration;
	this.note = note;
	Json j;
	hpRhs = json!=null && (j=json.get("hp"))!=null && j.booleanValue();
	tieLhs = json!=null && (j=json.get("tie"))!=null && j.booleanValue();
	tieRhs = false;
	slide = json!=null&&(j=json.get("slide"))!=null?j.stringValue():null;
	ghost = json!=null && (j=json.get("ghost"))!=null && j.booleanValue();
	dead = json!=null && (j=json.get("dead"))!=null && j.booleanValue();
	rest = json!=null && (j=json.get("rest"))!=null && j.booleanValue();
    }
    @Override public int compareTo(Event e){
	int i;
	if ((i=time.compareTo(e.time))!=0)
	    return i;
	if ((i=note.compareTo(e.note))!=0)
	    return i;
	if ((i=duration.compareTo(e.duration))!=0)
	    return i;
	i = 0;
	i += i+(hpRhs?1:0)-(e.hpRhs?1:0);
	i += i+(tieRhs?1:0)-(e.tieRhs?1:0);
	i += i+(tieLhs?1:0)-(e.tieLhs?1:0);
	i += i+(ghost?1:0)-(e.ghost?1:0);
	i += i+(dead?1:0)-(e.dead?1:0);
	return i;
    }
    void getAdjectives(Set<String>adjectives){
	if (ghost)
	    adjectives.add("\\parenthesize");
	if (dead)
	    adjectives.add("\\deadNote");
    }
    void getAfterAdjectives(Set<String>afterAdjectives){
	if ("legato".equals(slide))
	    afterAdjectives.add("\\glissando");
	note.getAfterAdjectives(afterAdjectives);
    }
    Event tie(Event rhs){
	return new Event(this,time,rhs.time.add(rhs.duration).subtract(time),tieLhs,rhs.tieRhs,rhs.hpRhs,note.tie(rhs.note));
    }
    Event[]split(Rational d){
	if (duration.compareTo(d)<=0)
	    return new Event[]{this,null};
	Note[]n=note.split();
	if (n[1]==null)
	    return new Event[]{new Event(this,time,d,tieLhs,false,hpRhs,n[0]),null};
	Event lhs=new Event(this,time,d,tieLhs,true,false,n[0]);
	Event rhs=new Event(this,time.add(d),duration.subtract(d),true,tieRhs,hpRhs,n[1]);
	return new Event[]{lhs,rhs};
    }
    String tieString(){
	return note.tieString()+','+time.add(duration);
    }
    boolean isJunk(){
	return state.argv_no_ghost_notes&&ghost || state.argv_output_text&&!(note instanceof TextNote);
    }
}
