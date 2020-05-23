import java.util.*;

final class LyricsInstrument extends Instrument{
    @Override boolean matches(State state){
	return state.lyrics;
    }
    @Override void printHead(){
	indent(state.partName+" = \\lyricmode {"); //}
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
    @Override String notesToString(List<Event>list){
	StringBuilder sb=new StringBuilder();
	int time=0;
	for (int i=0; i<list.size();){
	    int start=list.get(i).time;
	    if (start!=time)
		sb.append(appendTime("\\skip ",start-time)).append(' ');
	    int j=i;
	    int duration=list.get(i).duration;
	    while (++i<list.size()&&list.get(i).time==start)
		if (list.get(i).duration!=duration)
		    throw new RuntimeException();
	    if (i>j+1){
		sb.append("<<");
		for (int k=j; k<i; k++){
		    sb.append(" { ");
		    Event e=list.get(k);
		    sb.append(appendTime(e.toString(),duration));
		    sb.append(e.getLySuffix());
		    sb.append(" } ");
		}
		sb.append(">>");
	    }else{
		Event e=list.get(j);
		sb.append(appendTime(e.toString(),duration));
		sb.append(e.getLySuffix());
	    }
	    sb.append(' ');
	    time = start+duration;
	}
	if (time!=state.timen*DIVISION)
	    sb.append(appendTime("\\skip ",state.timen*DIVISION-time)).append(' ');
	sb.append('|');
	return sb.toString();
    }
    @Override void engrave(Json measure,Json nextMeasure){
	Json index=measure.get("index");
	Json lyrics=state.data.get("lyrics").get(index.intValue()-1);
	List<Event>list=new ArrayList<Event>();
	int time=0;
	for (Json beat:lyrics.get("beats").list){
	    int duration=getDuration(beat.get("duration"));
	    for (Json note:beat.get("lyrics").list)
		list.add(new Event(time,duration,note));
	    time += duration;
	}
	print(notesToString(list));
    }
}
