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
    private String getRelativeNote(Note note){
	if (note instanceof MidiNote)
	    return Stuff.midi2ly(((MidiNote)note).note,state);
	return note.getLyNote();
    }
    @Override Rational notesToString(List<Event>l,Rational duration){
	if (l.size()==1){
	    Event e=l.get(0);
	    return appendDuration(e.getAdjectives()+getRelativeNote(e.note),e.note.getLySuffix(),duration,e.tieLhs,false);
	}
	boolean allTies=true;
	for (Event e:l)
	    allTies &= e.tieLhs;
	StringBuilder sb2=new StringBuilder();
	sb2.append(lt);
	int lastRelative=0;
	for (int i=0; i<l.size(); i++){
	    if (i!=0)
		sb2.append(between);
	    Event e=l.get(i);
	    sb2.append(e.getAdjectives()).append(getRelativeNote(e.note)).append(e.note.getLySuffix());
	    if (e.tieLhs && !allTies)
		sb2.append('~');
	    if (i==0)
		lastRelative = state.lastRelative;
	}
	state.lastRelative = lastRelative;
	sb2.append(gt);
	Rational remaining=appendDuration(sb2.toString(),"",duration,allTies,false);
	return state.argv_lyrics?Rational.ZERO:remaining;
    }
}
