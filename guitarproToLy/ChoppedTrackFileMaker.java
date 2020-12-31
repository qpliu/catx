import java.io.*;
import java.util.*;

class ChoppedTrackFileMaker extends TrackFileMaker{
    final Class clazz;
    final MeasureMaker.GetWhatSuffix skip;
    ChoppedTrackFileMaker(Main main,Arg arg,String fn,String suffix,String lyname,Class clazz,MeasureMaker.GetWhatSuffix skip)throws IOException{
	super(main,arg,fn,suffix,lyname);
	this.clazz = clazz;
	this.skip = skip;
    }
    @Override String makeMeasure(Gpfile.Measure measure,List<Gpfile.Event>events)throws IOException{
	Queue<Gpfile.Event>q=new PriorityQueue<Gpfile.Event>();
	for (Gpfile.Event e:events)
	    if (clazz.isInstance(e))
		q.add(e);
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
	    mm.make(e.time,skip);
	    mm.make(end,getGetWhatSuffix(l));
	}
	mm.make(measure.time.add(measure.time_n),skip);
	return mm.tail();
    }
    MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	return null;
    }
}
