import java.math.*;
import java.util.*;

final class NotesToStringTabs extends NotesToString{
    private String lastTab;
    private String lastString;
    NotesToStringTabs(State state){
	super(state,"r");
    }
    @Override Rational notesToString(List<Event>l,Rational duration){
	boolean allTies=true;
	for (Event f:l)
	    allTies &= f.tieLhs;
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
	String what=sb2.toString();
	if ((firstString+what).equals(lastTab))
	    what = "t";
	else{
	    lastTab = firstString+what;
	    if (!firstString.equals(lastString)){
		lastString = firstString;
		what = firstString+what;
	    }
	}
	return appendDuration(what,"",duration,allTies,false);
    }
}
