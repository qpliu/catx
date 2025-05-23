import java.io.*;
import java.util.*;

final class DotextFileMaker extends ChoppedTrackFileMaker{
    static final String FILENAME_SUFFIX="_dotext";
    DotextFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg,arg.name+FILENAME_SUFFIX,"",arg.name+"Dotext");
    }
    @Override boolean filterEvents(Gpfile.Event event){
	return event instanceof Gpfile.DotextEvent;
    }
    @Override void midi(MusicFileMaker mfm)throws IOException{
	String fn=main.globalarg.modified_filename.contains(original_filename)?original_filename:filename;
	if (arg.midi_who!=null)
	    mfm.print("\\tag #'("+arg.midi_who+") \\new "+getStaffType()+" = "+Stuff.quote(fn)+" \\"+lyname);
    }
    @Override void printInclude(FileMaker fm){
	String done=filename.substring(0,filename.length()-7)+"_donetext";
	fm.print("\\include "+Stuff.quote(done));
    }
    @Override void layout(MusicFileMaker mfm)throws IOException{}
    @Override MeasureMaker.GetWhatSuffix getRestGetWhatSuffix(){
	return MeasureMaker.SKIP;
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	boolean allTied=true;
	for (Gpfile.Event e:list)
	    allTied &= e.tie_lhs;
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
	    Gpfile.DotextEvent de=(Gpfile.DotextEvent)list.get(i);
	    what_notrhs.append('`').append(de.dotext).append('`');
	    what_rhs.append('`').append(de.dotext).append('`');
	    if (!allTied && de.tie_lhs)
		what_rhs.append('~');
	}
	if (list.size()!=1){
	    what_rhs.append('>');
	    what_notrhs.append('>');
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
}
