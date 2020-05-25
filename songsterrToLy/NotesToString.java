import java.math.*;
import java.util.*;

final class NotesToString{
    private final State state;
    private final StringBuilder sb=new StringBuilder();
    private Rational last_tuplet;
    private String lastDuration;
    NotesToString(State state){
	this.state = state;
    }
    private void appendDuration(String what,String suffix,Rational duration,boolean tieLhs){
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
	    appendDuration(what,suffix,duration.multiply(tuplet),tieLhs);
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
	    sb.append(what).append(n);
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
		sb.append(' ');
		appendDuration(what,suffix,duration,tieLhs);
	    }else if (tieLhs)
		sb.append('~');
	}
    }
    String notesToString(PriorityQueue<Event>q,String rest,String lt,String gt,String between){
	Rational measureEndTime=state.measureStartTime.add(state.time_n);
	Rational time=state.measureStartTime;
	while (q.size()!=0){
	    Event e=q.poll();
	    if (!e.time.equals(time)){
		appendDuration(rest,"",e.time.subtract(time),false);
		sb.append(' ');
	    }
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
		    q.add(new Event(f,f.time.add(duration),f.duration.subtract(duration)));
		    f.tieLhs = true;
		}
	    if (l.size()==1)
		appendDuration(e.toString(),e.note.getLySuffix(),duration,e.tieLhs);
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
		appendDuration(sb2.toString(),"",duration,allTies);
	    }
	    sb.append(' ');
	    time = e.time.add(duration);
	}
	if (!time.equals(measureEndTime)){
	    appendDuration(rest,"",measureEndTime.subtract(time),false);
	    sb.append(' ');
	}
	if (last_tuplet!=null)
	    sb.append(/*{*/"} ");
	sb.append('|');
	return sb.toString();
    }
}
