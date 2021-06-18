import java.io.*;
import java.util.*;

abstract class ChoppedTrackFileMaker extends TrackFileMaker{
    ChoppedTrackFileMaker(Main main,Arg arg,String fn,String suffix,String lyname)throws IOException{
	super(main,arg,fn,suffix,lyname);
    }
    final Queue<Gpfile.Event>getFilteredEvents(Collection<Gpfile.Event>events){
	Queue<Gpfile.Event>q=new PriorityQueue<Gpfile.Event>();
	for (Gpfile.Event e:events)
	    if (filterEvents(e))
		q.add(e);
	return q;
    }
    boolean filterEvents(Gpfile.Event event){
	return true;
    }
    @Override String makeMeasure(Gpfile.Measure measure,List<Gpfile.Event>measureEvents)throws IOException{
	Log.debug("ChoppedTrackFileMaker.makeMeasure track=%s measure=%s",track,measure.name);
	Queue<Gpfile.Event>q=getFilteredEvents(measureEvents);
	MeasureMaker mm=new MeasureMaker(measure);
	for (Gpfile.Event e; (e=q.peek())!=null;){
	    Rational end=e.time.add(e.duration);
	    List<Gpfile.Event>l=new ArrayList<Gpfile.Event>();
	    while (q.size()!=0){
		Gpfile.Event f=q.peek();
		if (!e.time.equals(f.time)){
		    if (end.compareTo(f.time)>0)
			end = f.time;
		    break;
		}
		l.add(q.poll());
	    }
	    for (int i=0; i<l.size(); i++){
		Gpfile.Event[]ee=Stuff.cutEvent(l.get(i),end);
		if (ee[1]!=null)
		    q.add(ee[1]);
		l.set(i,ee[0]);
	    }
	    Collections.sort(l);
	    mm.make(e.time,getRestGetWhatSuffix());
	    mm.make(end,getGetWhatSuffix(l));
	}
	mm.make(measure.time.add(measure.time_n),getRestGetWhatSuffix());
	return mm.tail();
    }
    abstract MeasureMaker.GetWhatSuffix getRestGetWhatSuffix();
    abstract MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list);
}
