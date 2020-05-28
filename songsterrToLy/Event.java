import java.math.*;
import java.util.*;

final class Event implements Comparable<Event>{
    final Rational time;
    final Rational duration;
    final Note note;
    final boolean ghost;
    final boolean dead;
    final boolean tieLhs;
    final boolean tieRhs;
    final boolean rest;
    private Event(Event e,Rational time,Rational duration,boolean tieLhs,boolean tieRhs,Note note){
	this.time = time;
	this.duration = duration;
	this.note = note;
	ghost = e.ghost;
	dead = e.dead;
	this.tieLhs = tieLhs;
	this.tieRhs = tieRhs;
	rest = e.rest;
    }
    Event(Rational time,Rational duration,Json json,Note note){
	this.time = time;
	this.duration = duration;
	this.note = note;
	Json j;
	tieLhs = (j=json.get("tie"))!=null && j.booleanValue();
	tieRhs = false;
	ghost = (j=json.get("ghost"))!=null && j.booleanValue();
	dead = (j=json.get("dead"))!=null && j.booleanValue();
	rest = (j=json.get("rest"))!=null && j.booleanValue();
    }
    @Override public int compareTo(Event e){
	int i;
	if ((i=time.compareTo(e.time))!=0)
	    return i;
	if ((i=note.compareTo(e.note))!=0)
	    return i;
	if ((i=duration.compareTo(e.duration))!=0)
	    return i;
	return (tieRhs?8:0)-(e.tieRhs?8:0)+(tieLhs?4:0)-(e.tieLhs?4:0)+(ghost?2:0)-(e.ghost?2:0)+(dead?1:0)-(e.dead?1:0);
    }
    String getAdjectives(){
	StringBuilder sb=new StringBuilder();
	if (ghost)
	    sb.append("\\parenthesize ");
	if (dead)
	    sb.append("\\deadNote ");
	return sb.toString();
    }
    Event tie(Event rhs){
	return new Event(this,time,rhs.time.add(rhs.duration).subtract(time),tieLhs,rhs.tieRhs,note.tie(rhs.note));
    }
    Event[]split(Rational d){
	if (duration.compareTo(d)<=0)
	    return new Event[]{this,null};
	Note[]n=note.split();
	if (n[1]==null)
	    return new Event[]{new Event(this,time,d,tieLhs,false,n[0]),null};
	Event lhs=new Event(this,time,d,tieLhs,true,n[0]);
	Event rhs=new Event(this,time.add(d),duration.subtract(d),true,tieRhs,n[1]);
	return new Event[]{lhs,rhs};
    }
    String tieString(){
	return note.tieString()+','+time.add(duration);
    }
}
