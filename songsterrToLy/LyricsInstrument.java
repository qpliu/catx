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
	public int compareTo(Note n){
	    return text.compareTo(((LyricNote)n).text);
	}
	@Override public String getLyNote(){
	    StringBuilder sb=new StringBuilder("\"");
	    for (char c:text.toCharArray()){
		if (c=='"' || c=='\\')
		    sb.append('\\');
		sb.append(c);
	    }
	    return sb.append('"').toString();
	}
    }
    @Override Note getNote(Json note){
	return new LyricNote(note.get("text").stringValue());
    }
    @Override String notesToString(PriorityQueue<Event>q){
	return notesToString(q,"\\skip","<< { "," } >>"," } { ");
    }
    @Override void engrave(Json measure){
	if (state.pass==0){
	    Json index=measure.get("index");
	    Json lyrics=state.data.get("lyrics").get(index.intValue()-1);
	    Rational time=Rational.ZERO;
	    for (Json beat:lyrics.get("beats").list){
		Rational duration=getDuration(beat.get("duration"));
		for (Json note:beat.get("lyrics").list)
		    events.add(new Event(state.measureStartTime.add(time),duration,note));
		time = time.add(duration);
	    }
	}else
	    super.engrave(measure);
    }
}
