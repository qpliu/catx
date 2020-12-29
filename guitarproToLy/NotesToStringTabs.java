import java.math.*;
import java.util.*;

final class NotesToStringTabs extends NotesToString{
    private String lastTab;
    private String lastString;
    NotesToStringTabs(State state){
	super(state,"r");
    }
    @Override void notesToString(List<Event>l,String duration){
	boolean allTies=true;
	for (Event f:l)
	    allTies &= f.tieRhs;
	StringBuilder sb2=new StringBuilder();
	Collections.sort(l,(a,b)->((GuitarInstrument.GuitarNote)b.note).string-((GuitarInstrument.GuitarNote)a.note).string);
	int string=((GuitarInstrument.GuitarNote)l.get(0).note).string;
	String firstString=string+"-";
	Set<String>afterAdjectives=new TreeSet<String>();
	for (int i=0; i<l.size(); i++){
	    Event f=l.get(i);
	    GuitarInstrument.GuitarNote gn=(GuitarInstrument.GuitarNote)f.note;
	    if (gn.string==string-1 && i!=0)
		sb2.append('.');
	    else if (gn.string!=string)
		sb2.append(gn.string).append('-');
	    string = gn.string-1;
	    Set<String>adjectives=new TreeSet<String>();
	    f.getAdjectives(adjectives);
	    f.getAfterAdjectives(afterAdjectives);
	    if (adjectives.remove("\\parenthesize"))
		sb2.append('g');
	    if (adjectives.remove("\\deadNote"))
		sb2.append('x');
	    if (adjectives.size()!=0){
		sb2.append('"');
		for (String a:adjectives)
		    sb2.append(a).append(' ');
		sb2.setLength(sb2.length()-1);
		sb2.append('"');
	    }
	    sb2.append(gn.fret);
	    if (f.tieRhs && !allTies)
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
	sb.append(what).append(duration);
	if (allTies)
	    sb.append('~');
	else
	    for (String a:afterAdjectives)
		sb.append(' ').append(a);
    }
}
