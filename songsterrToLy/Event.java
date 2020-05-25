import java.math.*;
import java.util.*;

final class Event implements Comparable<Event>{
    final Rational time;
    Rational duration;
    final Note note;
    final boolean ghost;
    final boolean dead;
    final boolean tieRhs;
    boolean tieLhs;
    final boolean rest;
    Event(Event e,Rational time,Rational duration){
	this.time = time;
	this.duration = duration;
	note = e.note;
	ghost = e.ghost;
	dead = e.dead;
	tieRhs = e.tieRhs;
	tieLhs = e.tieLhs;
	rest = e.rest;
    }
    Event(Rational time,Rational duration,Json json,Note note){
	this.time = time;
	this.duration = duration;
	this.note = note;
	Json j;
	tieRhs = (j=json.get("tie"))!=null && j.booleanValue();
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
	return (tieLhs?4:0)-(e.tieLhs?4:0)+(ghost?2:0)-(e.ghost?2:0)+(dead?1:0)-(e.dead?1:0);
    }
    String getAdjectives(){
	StringBuilder sb=new StringBuilder();
	if (ghost)
	    sb.append("\\parenthesize ");
	if (dead)
	    sb.append("\\deadNote ");
	return sb.toString();
    }
}
