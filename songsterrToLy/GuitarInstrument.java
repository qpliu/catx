import java.util.*;

final class GuitarInstrument extends Instrument{
    int[]tuning;
    int offset_tuning;
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
	@Override public String getLyNote(){
	    if (state.argv_output_text){
		StringBuilder sb=new StringBuilder();
		sb.append("`.(").append(Stuff.midi2ly(note)).append(")bendSongsterr");
		List<Json>l=bend.get("points").list;
		for (int i=0; i<l.size(); i++){
		    if (i!=0)
			sb.append(':');
		    sb.append(l.get(i).get("position").intValue()).append(':').append(l.get(i).get("tone").intValue());
		}
		sb.append('`');
		return sb.toString();
	    }
	    return super.getLyNote();
	}
	@Override public boolean isJunk(){
	    return state.argv_output_text && bend==null;
	}
    }
    @Override void setState(State state){
	super.setState(state);
	List<Json>l=state.track.get("tuning").list;
	tuning = new int[l.size()];
	for (int i=0; i<tuning.length; i++)
	    tuning[i] = l.get(i).intValue()+offset_tuning;
	offset_tuning = -12;
	Json j;
	if ((j=state.track.get("isBassGuitar"))!=null && j.booleanValue())
	    offset_tuning = 0;
	if ((j=state.track.get("isGuitar"))!=null && j.booleanValue())
	    offset_tuning = 0;
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
