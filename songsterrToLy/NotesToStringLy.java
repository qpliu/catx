import java.math.*;
import java.util.*;

final class NotesToStringLy extends NotesToString{
    final String lt;
    final String between;
    final String gt;
    NotesToStringLy(State state,String rest,String lt,String between,String gt){
	super(state,rest);
	this.lt = lt;
	this.between = between;
	this.gt = gt;
    }
    @Override void notesToString(List<Event>l,String duration){
	boolean allTies=true;
	for (Event e:l)
	    allTies &= e.tieRhs;
	Set<String>afterAdjectives=new TreeSet<String>();
	if (l.size()==1){
	    Event e=l.get(0);
	    Set<String>adjectives=new TreeSet<String>();
	    e.getAdjectives(adjectives);
	    for (String a:adjectives)
		sb.append(a).append(' ');
	    sb.append(e.note.getLyNote());
	    sb.append(duration);
	    sb.append(e.note.getLySuffix());
	    e.getAfterAdjectives(afterAdjectives);
	}else{
	    sb.append(lt);
	    int lastRelative=0;
	    for (int i=0; i<l.size(); i++){
		if (i!=0)
		    sb.append(between);
		Event e=l.get(i);
		Set<String>adjectives=new TreeSet<String>();
		e.getAdjectives(adjectives);
		for (String a:adjectives)
		    sb.append(a).append(' ');
		e.getAfterAdjectives(afterAdjectives);
		sb.append(e.note.getLyNote());
		sb.append(e.note.getLySuffix());
		if (e.tieRhs && !allTies)
		    sb.append('~');
		if (i==0)
		    lastRelative = state.lastRelative;
	    }
	    state.lastRelative = lastRelative;
	    sb.append(gt);
	    sb.append(duration);
	}
	if (allTies)
	    sb.append('~');
	else
	    for (String a:afterAdjectives)
		sb.append(' ').append(a);
    }
}
