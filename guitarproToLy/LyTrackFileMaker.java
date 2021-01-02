import java.io.*;
import java.util.*;

final class LyTrackFileMaker extends SuperTrackFileMaker{
    private boolean last_hammer;
    LyTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,"",arg,MeasureMaker.REST);
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	boolean allTied=true;
	boolean is_hammer=false;
	boolean is_slide=false;
	Gpfile.Bend bend=null;
	for (Gpfile.Event e:list){
	    Gpfile.NoteEvent ne=(Gpfile.NoteEvent)e;
	    allTied &= ne.tie_lhs;
	    is_hammer |= ne.is_hammer;
	    is_slide |= ne.slide!=null;
	    if (ne.bend!=null)
		bend = ne.bend;
	}
	StringBuilder what_notrhs=new StringBuilder();
	StringBuilder what_rhs=new StringBuilder();
	StringBuilder suffix_notrhs=new StringBuilder();
	StringBuilder suffix_rhs=new StringBuilder();
	if (list.size()!=1){
	    what_notrhs.append('<');
	    what_rhs.append('<');
	}
	for (int i=0; i<list.size(); i++){
	    if (i!=0){
		what_notrhs.append(' ');
		what_rhs.append(' ');
	    }
	    Gpfile.NoteEvent ne=(Gpfile.NoteEvent)list.get(i);
	    if (ne.is_dead){
		what_notrhs.append("\\deadNote ");
		what_rhs.append("\\deadNote ");
	    }
	    if (ne.is_ghost){
		what_notrhs.append("\\parenthesize ");
		what_rhs.append("\\parenthesize ");
	    }
	    String ly=Stuff.midi2ly(ne.getNote(),arg);
	    what_notrhs.append(ly);
	    what_rhs.append(ly);
	    if (arg.string_numbers && list.size()!=1){
		what_rhs.append('\\').append(ne.string+1);
		what_notrhs.append('\\').append(ne.string+1);
	    }
	    if (!allTied && ne.tie_lhs)
		what_rhs.append('~');
	}
	if (list.size()!=1){
	    what_rhs.append('>');
	    what_notrhs.append('>');
	}
	if (arg.string_numbers && list.size()==1){
	    Gpfile.NoteEvent ne=(Gpfile.NoteEvent)list.get(0);
	    suffix_rhs.append('\\').append(ne.string+1);
	    suffix_notrhs.append('\\').append(ne.string+1);
	}
	suffix_notrhs.append('~');
	if (allTied)
	    suffix_rhs.append('~');
	if (!last_hammer && is_hammer)
	    suffix_rhs.append('(');
	if (last_hammer && !is_hammer)
	    suffix_rhs.append(')');
	if (is_slide)
	    suffix_rhs.append("\\glissando");
	if (bend!=null){
	    int peak=0;
	    for (int i=0; i<bend.y.length; i++)
		if (Math.abs(bend.y[i])>Math.abs(peak))
		    peak = bend.y[i];
	    if (peak>87)
		suffix_rhs.append("\\bendAfter #6 ^\"full\"");
	    else if (peak>62)
		suffix_rhs.append("\\bendAfter #6 ^\"\u00be\"");
	    else if (peak>37)
		suffix_rhs.append("\\bendAfter #6 ^\"\u00bd\"");
	    else if (peak>12)
		suffix_rhs.append("\\bendAfter #6 ^\"\u00bc\"");
	    if (peak<-87)
		suffix_rhs.append("\\bendAfter #-6 _\"full\"");
	    else if (peak<-62)
		suffix_rhs.append("\\bendAfter #-6 _\"\u00be\"");
	    else if (peak<-37)
		suffix_rhs.append("\\bendAfter #-6 _\"\u00bd\"");
	    else if (peak<-12)
		suffix_rhs.append("\\bendAfter #-6 _\"\u00bc\"");
	}
	last_hammer = is_hammer;
	return new MeasureMaker.GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		return is_rhs?what_rhs.toString():what_notrhs.toString();
	    }
	    @Override public String getSuffix(boolean is_lhs,boolean is_rhs){
		return is_rhs?suffix_rhs.toString():suffix_notrhs.toString();
	    }
	};
    }
}
