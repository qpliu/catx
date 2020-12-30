import java.io.*;
import java.util.*;

final class LyTrackFileMaker extends SuperTrackFileMaker{
    LyTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,"",arg);
    }
    @Override void makeMeasure(Gpfile.Measure measure,PriorityQueue<Gpfile.Event>events)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	while (events.size()!=0){
	    Gpfile.Event e=events.poll();
	    if (!(e instanceof Gpfile.NoteEvent))
		continue;
	    Gpfile.NoteEvent ne=(Gpfile.NoteEvent)e;
	    mm.rest(ne.time);
	    List<Gpfile.NoteEvent>l=new ArrayList<Gpfile.NoteEvent>();
	    l.add(ne);
	    Rational shortest=ne.duration;
	    while (events.size()!=0 && events.peek().time.equals(ne.time)){
		Gpfile.Event e2=events.poll();
		if (!(e2 instanceof Gpfile.NoteEvent))
		    continue;
		Gpfile.NoteEvent ne2=(Gpfile.NoteEvent)e2;
		if (ne2.duration.compareTo(shortest)<0)
		    shortest = ne2.duration;
		l.add(ne2);
	    }
	    for (int i=0; i<l.size(); i++){
		Gpfile.NoteEvent ne2=l.get(i);
		Gpfile.Event[]ee=Stuff.cutEvent(ne2,ne.time.add(shortest));
		if (ee[1]!=null)
		    events.add(ee[1]);
		l.set(i,(Gpfile.NoteEvent)ee[0]);
	    }
	    mm.make(ne.time.add(shortest),chordToGws(l),false);
	}
	mm.rest(measure.time.add(measure.time_n));
	print(mm.tail());
    }
    MeasureMaker.GetWhatSuffix chordToGws(List<Gpfile.NoteEvent>l){
	boolean allTied=true;
	for (Gpfile.NoteEvent ne:l)
	    allTied &= ne.tie_lhs;
	StringBuilder what_notrhs=new StringBuilder();
	StringBuilder what_rhs=new StringBuilder();
	StringBuilder suffix_notrhs=new StringBuilder();
	StringBuilder suffix_rhs=new StringBuilder();
	if (l.size()!=1){
	    what_notrhs.append('<');
	    what_rhs.append('<');
	}
	for (int i=0; i<l.size(); i++){
	    if (i!=0){
		what_notrhs.append(' ');
		what_rhs.append(' ');
	    }
	    Gpfile.NoteEvent ne=l.get(i);
	    what_notrhs.append(noteEventToLy(ne));
	    what_rhs.append(noteEventToLy(ne));
	    if (arg.string_numbers && l.size()!=1){
		what_rhs.append('\\').append(ne.string);
		what_notrhs.append('\\').append(ne.string);
	    }
	    if (!allTied && ne.tie_lhs)
		what_rhs.append('~');
	}
	if (l.size()!=1){
	    what_rhs.append('>');
	    what_notrhs.append('>');
	}
	if (arg.string_numbers && l.size()==1){
	    Gpfile.NoteEvent ne=l.get(0);
	    suffix_rhs.append('\\').append(ne.string+1);
	    suffix_notrhs.append('\\').append(ne.string+1);
	}
	suffix_notrhs.append('~');
	if (allTied)
	    suffix_rhs.append('~');
	return new MeasureMaker.GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		return is_rhs?what_rhs.toString():what_notrhs.toString();
	    }
	    @Override public String getSuffix(boolean is_lhs,boolean is_rhs){
		return is_rhs?suffix_rhs.toString():suffix_notrhs.toString();
	    }
	};
    }
    String noteEventToLy(Gpfile.NoteEvent ne){
	return Stuff.midi2ly(track.tuning[ne.string]+ne.fret,arg);
    }
}
