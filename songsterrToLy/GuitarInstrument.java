import java.util.*;

final class GuitarInstrument extends Instrument{
    int[]tuning;
    private class GuitarNote extends MidiNote{
	final int string;
	GuitarNote(int string,int fret){
	    super(tuning[string]+fret);
	    this.string = string;
	}
	@Override public String getLySuffix(){
	    return "\\"+(string+1);
	}
    }
    @Override void setState(State state){
	super.setState(state);
	List<Json>l=state.track.get("tuning").list;
	tuning = new int[l.size()];
	StringBuilder sb=new StringBuilder();
	for (int i=tuning.length; --i>=0;){
	    tuning[i] = l.get(i).intValue();
	    sb.append(' ').append(Stuff.midi2ly(tuning[i]));
	}
	noindent("% tuning"+sb);
    }
    @Override Note getNote(Json note){
	Json string=note.get("string");
	Json fret=note.get("fret");
	if (string==null || fret==null)
	    return null;
	return new GuitarNote(string.intValue(),fret.intValue());
    }
    @Override boolean matches(State state){
	return true;
    }
    @Override void printHead(){
	indent(state.partName+" = {"); //}
    }
}
