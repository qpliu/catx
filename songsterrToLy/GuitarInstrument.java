import java.util.*;

final class GuitarInstrument extends Instrument{
    int[]tuning;
    class GuitarNote extends MidiNote{
	final int string;
	final int fret;
	final Json bend;
	GuitarNote(int string,int fret,Json bend){
	    super(GuitarInstrument.this.state,tuning[string-1]+fret);
	    this.string = string;
	    this.fret = fret;
	    this.bend = bend;
	}
	@Override public String getLySuffix(){
	    if (state.argv_no_string_numbers)
		return "";
	    return "\\"+string;
	}
	@Override public String tieString(){
	    return String.valueOf(string);
	}
	@Override public String getAfterAdjectives(){
	    StringBuilder sb=new StringBuilder();
	    sb.append(super.getAfterAdjectives());
	    if (bend!=null)
		sb.append("\\bendAfter #"+bend.get("tone").intValue()*.01);
	    return sb.toString();
	}
    }
    @Override void setState(State state){
	super.setState(state);
	List<Json>l=state.track.get("tuning").list;
	tuning = new int[l.size()];
	for (int i=0; i<tuning.length; i++)
	    tuning[i] = l.get(i).intValue();
    }
    @Override void engrave(Json measure){
	if (state.measureNumber==1){
	    StringBuilder sb=new StringBuilder();
	    for (int i=tuning.length; --i>=0;)
		sb.append(' ').append(Stuff.midi2ly(tuning[i]));
	    noindent("% tuning"+sb);
	}
	super.engrave(measure);
    }
    @Override Note getNote(Json note){
	Json string=note.get("string");
	Json fret=note.get("fret");
	if (string==null || fret==null)
	    return null;
	return new GuitarNote(string.intValue()+1,fret.intValue(),note.get("bend"));
    }
    @Override boolean matches(State state){
	return true;
    }
}
