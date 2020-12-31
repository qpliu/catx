import java.util.*;

final class Twiddler{
    final Arg arg;
    final Gpfile gpfile;
    Twiddler(Arg arg,Gpfile gpfile){
	this.arg = arg;
	this.gpfile = gpfile;
    }
    void twiddle(){
	for (Gpfile.Track track:gpfile.tracks){
	    joinTies(track);
	    makeSlide(track);
	}
    }
    private void joinTies(Gpfile.Track track){
	Gpfile.NoteEvent[]a=new Gpfile.NoteEvent[track.tuning.length];
	PriorityQueue<Gpfile.Event>q=new PriorityQueue<Gpfile.Event>(track.events);
	track.events.clear();
	for (Gpfile.Event e:q)
	    if (e instanceof Gpfile.NoteEvent){
		Gpfile.NoteEvent n=(Gpfile.NoteEvent)e;
		if (n.is_tie){
		    if (a[n.string]==null)
			Log.info("Strange tie track=%d start_time=%s string=%d",track.index,n.time,n.string);
		    else{
			Rational end=a[n.string].time.add(a[n.string].duration);
			if (!end.equals(n.time))
			    Log.info("Strange tie track=%d end_time=%s start_time=%s string=%d",track.index,end,n.time,n.string);
			else{
			    a[n.string].duration = a[n.string].duration.add(n.duration);
			    continue;
			}
		    }
		}
		if (a[n.string]!=null)
		    track.events.add(a[n.string]);
		a[n.string] = n;
	    }else
		track.events.add(e);
	for (Gpfile.Event e:a)
	    if (e!=null)
		track.events.add(e);
    }
    private void makeSlide(Gpfile.Track track){
	PriorityQueue<Gpfile.NoteEvent>q=new PriorityQueue<Gpfile.NoteEvent>();
	for (Gpfile.Event e:track.events)
	    if (e instanceof Gpfile.NoteEvent)
		q.add((Gpfile.NoteEvent)e);
	for (Gpfile.NoteEvent ne; (ne=q.poll())!=null;)
	    if (ne.slide!=null){
		int count=1;
		Rational end=ne.time.add(ne.duration);
		Rational slideEnd=ne.time.add(ne.duration);
		Rational lastTime=ne.time;
		while (q.size()!=0 && q.peek().time.compareTo(end)<=0){
		    Gpfile.NoteEvent e=q.poll();
		    Rational ee=e.time.add(e.duration);
		    if (e.slide!=null && ee.compareTo(end)>0)
			end = ee;
		    if (ee.compareTo(slideEnd)>0)
			slideEnd = ee;
		    if (!e.time.equals(lastTime)){
			lastTime = e.time;
			count++;
		    }
		}
		track.events.add(new Gpfile.DotextEvent(ne.time,slideEnd.subtract(ne.time),"slide"+count));
	    }
    }
}
