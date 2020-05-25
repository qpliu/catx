import java.math.*;
import java.util.*;

final class NotesToString{
    private final State state;
    private final StringBuilder sb=new StringBuilder();
    private Rational last_tuplet;
    private String lastDuration;
    private String lastTab;
    private String lastString;
    NotesToString(State state){
	this.state = state;
    }
    private void appendDuration(String what,String suffix,Rational duration,boolean tieLhs,boolean isTab){
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
	    appendDuration(what,suffix,duration.multiply(tuplet),tieLhs,isTab);
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
	    if (duration.signum()!=0){
		if (state.argv_lyrics)
		    what = "\\skip";
		if (!what.equals("r") && !what.equals("\\skip"))
		    sb.append('~');
		sb.append(' ');
		appendDuration(isTab?"t":what,suffix,duration,tieLhs,isTab);
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
		appendDuration(rest,"",e.time.subtract(time),false,false);
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
	    boolean outputTabs=state.argv_output_tabs;
	    for (Event f:l){
		if (!f.duration.equals(duration)){
		    q.add(new Event(f,f.time.add(duration),f.duration.subtract(duration)));
		    f.tieLhs = true;
		}
		outputTabs &= f.note instanceof GuitarInstrument.GuitarNote;
	    }
	    if (l.size()==1 && !outputTabs)
		appendDuration(e.getAdjectives()+e.note.getLyNote(),e.note.getLySuffix(),duration,e.tieLhs,false);
	    else{
		boolean allTies=true;
		for (Event f:l)
		    allTies &= f.tieLhs;
		String what;
		if (outputTabs){
		    StringBuilder sb2=new StringBuilder();
		    Collections.sort(l,(a,b)->((GuitarInstrument.GuitarNote)b.note).string-((GuitarInstrument.GuitarNote)a.note).string);
		    int string=((GuitarInstrument.GuitarNote)l.get(0).note).string;
		    String firstString=string+"-";
		    for (int i=0; i<l.size(); i++){
			Event f=l.get(i);
			GuitarInstrument.GuitarNote gn=(GuitarInstrument.GuitarNote)f.note;
			if (gn.string==string-1 && i!=0)
			    sb2.append('.');
			else if (gn.string!=string)
			    sb2.append(gn.string).append('-');
			string = gn.string-1;
			if (f.ghost)
			    sb2.append('g');
			if (f.dead)
			    sb2.append('x');
			sb2.append(gn.fret);
			if (f.tieLhs && !allTies)
			    sb2.append('~');
			sb2.append(i==l.size()-1?'t':'.');
		    }
		    what = sb2.toString();
		    if ((firstString+what).equals(lastTab))
			what = "t";
		    else{
			lastTab = firstString+what;
			if (!firstString.equals(lastString)){
			    lastString = firstString;
			    what = firstString+what;
			}
		    }
		    appendDuration(what,"",duration,allTies,true);
		}else{
		    StringBuilder sb2=new StringBuilder();
		    sb2.append(lt);
		    for (int i=0; i<l.size(); i++){
			if (i!=0)
			    sb2.append(between);
			Event f=l.get(i);
			sb2.append(f.getAdjectives()).append(f.note.getLyNote()).append(f.note.getLySuffix());
			if (f.tieLhs && !allTies)
			    sb2.append('~');
		    }
		    sb2.append(gt);
		    what = sb2.toString();
		    appendDuration(what,"",duration,allTies,false);
		}
	    }
	    sb.append(' ');
	    time = e.time.add(duration);
	}
	if (!time.equals(measureEndTime)){
	    appendDuration(rest,"",measureEndTime.subtract(time),false,false);
	    sb.append(' ');
	}
	if (last_tuplet!=null)
	    sb.append(/*{*/"} ");
	sb.append('|');
	return sb.toString();
    }
}
