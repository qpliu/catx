import java.util.*;

final class TextEventMaker{
    static void makeTextEvents(State state,PriorityQueue<Event>events){
	Map<String,Event>map=new HashMap<String,Event>();
	for (Event e:events)
	    map.put(e.note.tieString()+','+e.time,e);
	Set<Event>done=new HashSet<Event>();
	List<Event>l=new ArrayList<Event>(events);
	events.clear();
	for (Event e:l)
	    if (!done.contains(e)){
		if ("legato".equals(e.slide)){
		    Rational time=e.time;
		    Rational duration=e.duration;
		    int size=1;
		    for (Event f; (f=map.get(e.note.tieString()+','+e.time.add(e.duration)))!=null; e=f){
			done.add(f);
			duration = duration.add(f.duration);
			size++;
		    }
		    events.add(new Event(state,time,duration,null,new TextNote("slide"+size)));
		}else if (e.note instanceof GuitarInstrument.GuitarNote){
		    GuitarInstrument.GuitarNote n=(GuitarInstrument.GuitarNote)e.note;
		    if (n.bend!=null){
			StringBuilder sb=new StringBuilder();
			sb.append(".(").append(Stuff.midi2ly(n.note)).append(")bendSongsterr");
			List<Json>points=n.bend.get("points").list;
			for (int i=0; i<points.size(); i++){
			    if (i!=0)
				sb.append(':');
			    sb.append(points.get(i).get("position").intValue()).append(':').append(points.get(i).get("tone").intValue());
			}
			events.add(new Event(state,e.time,e.duration,null,new TextNote(sb.toString())));
		    }
		}
	    }
    }
}
