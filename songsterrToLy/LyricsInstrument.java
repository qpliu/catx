import java.util.*;

final class LyricsInstrument extends Instrument{
    @Override boolean matches(State state){
	return state.argv_lyrics;
    }
    @Override void printHead(){
	indent(state.argv_partName+" = \\new Lyrics \\lyricmode {"/*}*/);
    }
    static class LyricNote implements Note{
	private final String text;
	LyricNote(String text){
	    this.text = text;
	}
	@Override public int compareTo(Note n){
	    return text.compareTo(((LyricNote)n).text);
	}
	@Override public String getLyNote(){
	    return '"'+text.replace("\\","\\\\").replace("\"","\\\"")+'"';
	}
	@Override public Note[]split(){
	    return new Note[]{this,null};
	}
    }
    @Override Note getNote(Json note){
	return new LyricNote(note.get("text").stringValue());
    }
    @Override void engrave(Json measure){
	if (state.pass==0){
	    Json lyrics=state.data.get("lyrics").get(measure.get("index").intValue()-1);
	    Rational time=state.measureStartTime;
	    for (Json beat:lyrics.get("beats").list){
		Rational duration=getDuration(beat.get("duration"));
		for (Json note:beat.get("lyrics").list)
		    events.add(new Event(time,duration,note,getNote(note)));
		time = time.add(duration);
	    }
	}else
	    super.engrave(measure);
    }
}
