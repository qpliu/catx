import java.util.*;

final class Twiddler{
    final Arg arg;
    final Gpfile gpfile;
    Twiddler(Arg arg,Gpfile gpfile){
	this.arg = arg;
	this.gpfile = gpfile;
    }
    void twiddle(){
	for (Gpfile.Track track:gpfile.tracks)
	    joinTies(track);
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
			Log.error("Strange tie start_time=%s string=%d",n.time,n.string);
		    else{
			Rational end=a[n.string].time.add(a[n.string].duration);
			if (!end.equals(n.time))
			    Log.error("Strange tie end_time=%s start_time=%s string=%d",end,n.time,n.string);
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
}
