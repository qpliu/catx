import java.util.*;

final class Twiddler{
    final Main main;
    Twiddler(Main main){
	this.main = main;
    }
    void twiddle(){
	int tempo=main.gpfile.global_tempo;
	Gpfile.KeySignature key=main.gpfile.global_key;
	for (Gpfile.Measure measure:main.gpfile.measures){
	    if (measure.key==null)
		measure.key = key;
	    else
		key = measure.key;
	    if (measure.tempo<=0)
		measure.tempo = tempo;
	    else
		tempo = measure.tempo;
	}
	for (Gpfile.Track track:main.gpfile.tracks)
	    joinTies(track);
	addLyricEvents();
	twiddleRepeats();
    }
    void twiddleTrack(TrackFileMaker tfm){
	makeSlideDotext(tfm);
	makeBendDotext(tfm);
    }
    private void twiddleRepeats(){
	for (int i=0; i<main.gpfile.measures.length; i++)
	    if (main.gpfile.measures[i].repeatStart){
		int start=i;
		for (; i<main.gpfile.measures.length; i++)
		    if (main.gpfile.measures[i].repeatEnd>0){
			int end=i;
			main.gpfile.measures[start].twiddledRepeatStart = main.gpfile.measures[end].repeatEnd;
			int alternateCount=0;
			for (int j=start+1; j<=end; j++)
			    if (main.gpfile.measures[j].repeatAlternate>0)
				main.gpfile.measures[j].twiddledRepeatAlternate = ++alternateCount;
			if (main.gpfile.measures[end+1].repeatAlternate>0)
			    main.gpfile.measures[++end].twiddledRepeatAlternate = ++alternateCount;
			main.gpfile.measures[end].twiddledRepeatEnd = ++alternateCount;
			break;
		    }
	    }
    }
    private void joinTies(Gpfile.Track track){
	Gpfile.NoteEvent[]a=new Gpfile.NoteEvent[track.tuning.length];
	PriorityQueue<Gpfile.Event>q=new PriorityQueue<Gpfile.Event>(track.events);
	track.events.clear();
	for (Gpfile.Event e:q)
	    if (e instanceof Gpfile.NoteEvent){
		Gpfile.NoteEvent n=(Gpfile.NoteEvent)e;
		if (n.is_tie)
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
		if (a[n.string]!=null)
		    track.events.add(a[n.string]);
		a[n.string] = n;
	    }else
		track.events.add(e);
	for (Gpfile.Event e:a)
	    if (e!=null)
		track.events.add(e);
    }
    private void makeSlideDotext(TrackFileMaker tfm){
	PriorityQueue<Gpfile.NoteEvent>q=new PriorityQueue<Gpfile.NoteEvent>();
	for (Gpfile.Event e:tfm.trackEvents)
	    if (e instanceof Gpfile.NoteEvent)
		q.add((Gpfile.NoteEvent)e);
	Rational lastEnd=Rational.ZERO;
	for (Gpfile.NoteEvent ne; (ne=q.poll())!=null;)
	    if (ne.slide!=null && ne.time.compareTo(lastEnd)>=0){
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
		boolean continuousSlide=Stuff.canDoContinuousSlide(tfm.arg.midi_instrument);
		if (count>1)
		    tfm.trackEvents.add(new Gpfile.DotextEvent(ne.time,slideEnd.subtract(ne.time),continuousSlide?"slide":"slide1"));
		lastEnd = ne.time.add(ne.duration);
	    }
    }
    private void makeBendDotext(TrackFileMaker tfm){
	List<Gpfile.Event>newEvents=new ArrayList<Gpfile.Event>();
	Collections.sort(tfm.trackEvents);
	Rational lastEnd=Rational.ZERO;
	for (Gpfile.Event e:tfm.trackEvents)
	    if (e instanceof Gpfile.NoteEvent){
		Gpfile.NoteEvent ne=(Gpfile.NoteEvent)e;
		if (ne.bend!=null && ne.time.compareTo(lastEnd)>=0)
		    if (!tfm.arg.use_bend_end && !tfm.arg.use_bend_start){
			StringBuilder sb=new StringBuilder();
			sb.append(".(").append(Stuff.midi2ly(ne.getNote(),main.globalarg)).append(")bend");
			for (int i=0; i<ne.bend.x.length; i++){
			    if (i!=0)
				sb.append(':');
			    sb.append(ne.bend.x[i]).append(':').append(ne.bend.y[i]);
			}
			newEvents.add(new Gpfile.DotextEvent(ne.time,ne.duration,sb.toString()));
			lastEnd = ne.time.add(ne.duration);
		    }else{
			int bend=ne.bend.y[tfm.arg.use_bend_end?ne.bend.y.length-1:0];
			ne.bend = null;
			ne.fret += (bend+50000000+25)/50-1000000;
		    }
	    }
	tfm.trackEvents.addAll(newEvents);
    }
    private static String fix(String s){
	if (s.equals("+") || s.equals("_"))
	    return "";
	return s;
    }
    private void addLyricEvents(){
	for (Gpfile.TrackMeasureLyrics tml:main.gpfile.trackMeasureLyrics)
	    if (tml.track>=0){
		final Deque<Gpfile.LyricEvent>lyricq=new ArrayDeque<Gpfile.LyricEvent>();
		StringBuilder sb=new StringBuilder();
		int nest=0;
		for (int i=0; i<tml.lyrics.length(); i++){
		    char c=tml.lyrics.charAt(i);
		    if (c=='[')
			nest++;
		    sb.append(nest==0?c:' ');
		    if (c==']')
			--nest;
		}
		for (StringTokenizer st=new StringTokenizer(sb.toString()); st.hasMoreTokens();){
		    String s=st.nextToken();
		    boolean hyphen=false;
		    for (;;){
			int i=s.indexOf('-');
			if (i==-1){
			    lyricq.add(new Gpfile.LyricEvent(null,null,fix(s),false,hyphen,tml.which));
			    break;
			}
                        if (i > 0) {
                            lyricq.add(new Gpfile.LyricEvent(null,null,fix(s.substring(0,i)),true,hyphen,tml.which));
                        }
			hyphen = true;
			s = s.substring(i+1);
                        if (s.length() == 0) {
                            break;
                        }
		    }
		}
		Queue<Gpfile.Event>noteq=new PriorityQueue<Gpfile.Event>();
		Gpfile.Track track=main.gpfile.tracks[tml.track];
		Rational lastTime=main.gpfile.measures[tml.startingMeasure].time;
		Collections.sort(track.events);
		for (Gpfile.Event e:track.events)
		    if (e instanceof Gpfile.NoteEvent && e.time.compareTo(lastTime)>=0){
			noteq.add(e);
			lastTime = e.time.add(e.duration);
		    }
		for (Gpfile.Event ne; (ne=noteq.poll())!=null;){
		    Gpfile.LyricEvent le=lyricq.poll();
		    if (le==null)
			break;
		    le.time = ne.time;
		    le.duration = ne.duration;
		    while (le.hyphen_lhs && lyricq.size()!=0 && lyricq.peek().lyric.length()==0 && noteq.size()!=0){
			Gpfile.Event ee=noteq.poll();
			le.duration = ee.time.add(ee.duration).subtract(le.time);
			le.hyphen_lhs = lyricq.poll().hyphen_lhs;
		    }
		    while (noteq.size()!=0 && noteq.peek().time.equals(le.time))
			le.duration = noteq.poll().duration;
		    track.events.add(le);
		}
	    }
    }
}
