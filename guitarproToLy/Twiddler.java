import java.util.*;

final class Twiddler{
    final Arg arg;
    final Gpfile gpfile;
    Twiddler(Arg arg,Gpfile gpfile){
	this.arg = arg;
	this.gpfile = gpfile;
    }
    void twiddle(){
	int tempo=gpfile.global_tempo;
	Gpfile.KeySignature key=gpfile.global_key;
	for (Gpfile.Measure measure:gpfile.measures){
	    if (measure.key==null)
		measure.key = key;
	    else
		key = measure.key;
	    if (measure.tempo<=0)
		measure.tempo = tempo;
	    else
		tempo = measure.tempo;
	}
	for (Gpfile.Track track:gpfile.tracks){
	    joinTies(track);
	    makeSlide(track);
	    makeBend(track);
	}
	addLyricEvents();
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
    private void makeBend(Gpfile.Track track){
	List<Gpfile.Event>newEvents=new ArrayList<Gpfile.Event>();
	for (Gpfile.Event e:track.events)
	    if (e instanceof Gpfile.NoteEvent){
		Gpfile.NoteEvent ne=(Gpfile.NoteEvent)e;
		if (ne.bend!=null){
		    StringBuilder sb=new StringBuilder();
		    sb.append(".(").append(Stuff.midi2ly(ne.getNote(),arg)).append(")bendSongsterr");
		    for (int i=0; i<ne.bend.x.length; i++){
			if (i!=0)
			    sb.append(':');
			sb.append(ne.bend.x[i]).append(':').append(ne.bend.y[i]);
		    }
		    newEvents.add(new Gpfile.DotextEvent(ne.time,ne.duration,sb.toString()));
		}
	    }
	track.events.addAll(newEvents);
    }
    private void addLyricEvents(){
	for (Gpfile.TrackMeasureLyrics tml:gpfile.trackMeasureLyrics)
	    if (tml.track>=0){
		final Deque<Gpfile.LyricEvent>lyricq=new ArrayDeque<Gpfile.LyricEvent>();
		for (StringTokenizer st=new StringTokenizer(tml.lyrics); st.hasMoreTokens();){
		    String s=st.nextToken();
		    boolean hyphen=false;
		    for (;;){
			int i=s.indexOf('-');
			if (i==-1){
			    lyricq.add(new Gpfile.LyricEvent(null,null,s,false,hyphen,tml.which));
			    break;
			}
			lyricq.add(new Gpfile.LyricEvent(null,null,s.substring(0,i),true,hyphen,tml.which));
			hyphen = true;
			s = s.substring(i+1);
		    }
		}
		Queue<Gpfile.Event>noteq=new PriorityQueue<Gpfile.Event>();
		Gpfile.Track track=gpfile.tracks[tml.track];
		for (Gpfile.Event e:track.events)
		    if (e instanceof Gpfile.NoteEvent && e.time.compareTo(gpfile.measures[tml.startingMeasure].time)>=0)
			noteq.add(e);
		for (Gpfile.Event ne; (ne=noteq.poll())!=null;){
		    Gpfile.LyricEvent le=lyricq.poll();
		    if (le==null)
			break;
		    le.time = ne.time;
		    le.duration = ne.duration;
		    while (noteq.size()!=0 && noteq.peek().time.equals(le.time))
			le.duration = noteq.poll().duration;
		    track.events.add(le);
		}
	    }
    }
}
