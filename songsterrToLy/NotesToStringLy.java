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
	if (l.size()==1){
	    Event e=l.get(0);
	    sb.append(e.getAdjectives());
	    sb.append(e.note.getLyNote());
	    sb.append(duration);
	    sb.append(e.note.getLySuffix());
	    if (e.tieRhs)
		sb.append('~');
	}else{
	    boolean allTies=true;
	    for (Event e:l)
		allTies &= e.tieRhs;
	    sb.append(lt);
	    int lastRelative=0;
	    for (int i=0; i<l.size(); i++){
		if (i!=0)
		    sb.append(between);
		Event e=l.get(i);
		sb.append(e.getAdjectives());
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
	    if (allTies)
		sb.append('~');
	}
    }
}
