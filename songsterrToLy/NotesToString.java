import java.math.*;
import java.util.*;

abstract class NotesToString{
    final State state;
    final String rest;
    final StringBuilder sb=new StringBuilder();
    Rational tuplet=Rational.ONE;
    String lastDuration;
    NotesToString(State state,String rest){
	this.state = state;
	this.rest = rest;
    }
    static NotesToString get(State state){
	if (state.argv_output_tabs)
	    return new NotesToStringTabs(state);
	if (state.argv_lyrics)
	    return new NotesToStringLy(state,"\\skip","<< { "," } { "," } >>");
	return new NotesToStringLy(state,"r","<"," ",">");
    }
    private String getDuration(Rational[]duration,boolean skip){
	Rational t=new Rational(duration[0].d,BigInteger.ONE);
	while (!t.n.testBit(0))
	    t = new Rational(t.n.shiftRight(1),BigInteger.ONE);
	while (t.compareTo(Rational.TWO)>0)
	    t = t.divide(2);
	if (!t.equals(tuplet) && !tuplet.equals(Rational.ONE))
	    sb.append(/*{*/"} ");
	if (!t.equals(tuplet) && !t.equals(Rational.ONE))
	    sb.append("\\tuplet "+t+" { "); //}
	tuplet = t;
	duration[0] = duration[0].multiply(tuplet);
	Rational d=new Rational(state.time_d);
	BigInteger n=BigInteger.ONE;
	while (d.compareTo(duration[0])>0){
	    n = n.shiftLeft(1);
	    d = d.divide(2);
	}
	String z=n.toString();
	duration[0] = duration[0].subtract(d);
	if (duration[0].compareTo(d.divide(2))>=0){
	    z += '.';
	    duration[0] = duration[0].subtract(d.divide(2));
	}
	duration[0] = duration[0].divide(tuplet);
	if (skip)
	    return z;
	if (z.equals(lastDuration))
	    return "";
	lastDuration = z;
	return z;
    }
    final void appendRest(Rational duration){
	Rational[]dd=new Rational[]{duration};
	while (dd[0].signum()!=0){
	    String ds=getDuration(dd,rest.equals("\\skip"));
	    sb.append(rest).append(ds).append(' ');
	}
    }
    abstract void notesToString(List<Event>l,String duration);
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
	    Rational[]dd=new Rational[]{duration};
	    String ds=getDuration(dd,false);
	    duration = duration.subtract(dd[0]);
	    List<Event>l=new ArrayList<Event>();
	    for (Event f:ll){
		Event[]lr=f.split(duration);
		l.add(lr[0]);
		if (lr[1]!=null)
		    q.add(lr[1]);
	    }
	    notesToString(l,ds);
	    sb.append(' ');
	    time = e.time.add(duration);
	}
	if (!time.equals(measureEndTime))
	    appendRest(measureEndTime.subtract(time));
	if (!tuplet.equals(Rational.ONE))
	    sb.append(/*{*/"} ");
	sb.append('|');
	return sb.toString();
    }
}
