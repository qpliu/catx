import java.math.*;
import java.util.*;

abstract class NotesToString{
    final State state;
    final String rest;
    final StringBuilder sb=new StringBuilder();
    Rational last_tuplet;
    String lastDuration;
    NotesToString(State state,String rest){
	this.state = state;
	this.rest = rest;
    }
    static NotesToString get(State state){
	if (state.argv_output_text)
	    return new NotesToStringText(state);
	if (state.argv_output_tabs)
	    return new NotesToStringTabs(state);
	if (state.argv_lyrics)
	    return new NotesToStringLy(state,"\\skip","<< { "," } { "," } >>");
	return new NotesToStringLy(state,"r","<"," ",">");
    }
    final Rational appendDuration(String what,String suffix,Rational duration,boolean tieLhs,boolean isRest){
	if (duration.signum()<0)
	    throw new RuntimeException();
	BigInteger tuplet_d=duration.d;
	while (!tuplet_d.testBit(0))
	    tuplet_d = tuplet_d.shiftRight(1);
	if (!tuplet_d.equals(BigInteger.ONE)){
	    Rational tuplet=new Rational(tuplet_d,BigInteger.ONE);
	    while (tuplet.compareTo(Rational.TWO)>0)
		tuplet = tuplet.divide(2);
	    if (last_tuplet!=null && !tuplet.equals(last_tuplet))
		sb.append(/*{*/"} ");
	    if (!tuplet.equals(last_tuplet))
		sb.append("\\tuplet "+tuplet+" { "); //}
	    last_tuplet = null;
	    duration = appendDuration(what,suffix,duration.multiply(tuplet),tieLhs,isRest).divide(tuplet);
	    last_tuplet = tuplet;
	}else if (duration.signum()!=0){
	    if (last_tuplet!=null)
		sb.append(/*{*/"} ");
	    last_tuplet = null;
	    Rational d=new Rational(state.time_d);
	    BigInteger n=BigInteger.ONE;
	    while (d.compareTo(duration)>0){
		n = n.shiftLeft(1);
		d = d.divide(2);
	    }
	    String s=n.toString();
	    duration = duration.subtract(d);
	    if (duration.compareTo(d.divide(2))>=0){
		s += '.';
		duration = duration.subtract(d.divide(2));
	    }
	    sb.append(what);
	    if (what.equals("\\skip"))
		sb.append(s);
	    else if (!s.equals(lastDuration)){
		lastDuration = s;
		sb.append(s);
	    }
	    sb.append(suffix);
	    if (tieLhs || duration.signum()!=0 && !state.argv_lyrics && !isRest)
		sb.append('~');
	}
	return duration;
    }
    final void appendRest(Rational duration){
	while (duration.signum()!=0){
	    duration = appendDuration(rest,"",duration,false,true);
	    sb.append(' ');
	}
    }
    abstract Rational notesToString(List<Event>l,Rational duration);
    final String notesToString(PriorityQueue<Event>q){
	Rational measureEndTime=state.measureStartTime.add(state.time_n);
	Rational time=state.measureStartTime;
	while (q.size()!=0){
	    Event e=q.poll();
	    if (!e.time.equals(time))
		appendRest(e.time.subtract(time));
	    List<Event>ll=new ArrayList<Event>();
	    Rational duration=e.duration;
	    ll.add(e);
	    while (q.size()!=0 && q.peek().time.equals(e.time)){
		if (q.peek().duration.compareTo(duration)<0)
		    duration = q.peek().duration;
		ll.add(q.poll());
	    }
	    List<Event>l=new ArrayList<Event>();
	    for (Event f:ll){
		Event g=new Event(f,f.time,duration);
		g.tieLhs |= !f.duration.equals(duration);
		l.add(g);
	    }
	    duration = duration.subtract(notesToString(l,duration));
	    for (Event f:ll)
		if (!f.duration.equals(duration)){
		    Event g=new Event(f,f.time.add(duration),f.duration.subtract(duration));
		    g.tieRhs = true;
		    q.add(g);
		}
	    sb.append(' ');
	    time = e.time.add(duration);
	}
	if (!time.equals(measureEndTime))
	    appendRest(measureEndTime.subtract(time));
	if (last_tuplet!=null)
	    sb.append(/*{*/"} ");
	sb.append('|');
	return sb.toString();
    }
}
