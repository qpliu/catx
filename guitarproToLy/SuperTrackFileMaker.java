import java.io.*;
import java.util.*;

class SuperTrackFileMaker extends ChoppedTrackFileMaker{
    SuperTrackFileMaker(Main main,String suffix,Arg arg,Class clazz,MeasureMaker.GetWhatSuffix skip)throws IOException{
	super(main,arg,arg.name,suffix,arg.name,clazz,skip);
    }
    @Override final void layout(MusicFileMaker mfm)throws IOException{
	if (arg.layout_who==null)
	    return;
	mfm.indent("\\tag #'("+arg.layout_who+") \\new "+getStaffType()+" \\with {");
	if (arg.instrument_name!=null)
	    mfm.print("instrumentName = \\markup { \\rotate #90 \""+arg.instrument_name+"\" }");
	if (arg.instrument_short_name!=null)
	    mfm.print("shortInstrumentName = \\markup { \\rotate #90 \""+arg.instrument_short_name+"\" }");
	if (this instanceof DrumTrackFileMaker)
	    mfm.print("drumStyleTable = #(alist->hash-table myDrumStyleTable)");
	mfm.unindentindent("} <<");
	mfm.print("\\"+main.markupFileMaker.lyname);
	mfm.print('\\'+lyname);
	for (String s:arg.layout_extra)
	    mfm.print(s);
	mfm.unindent(">>");
	if (arg.layout_tabs){
	    mfm.indent("\\tag #'("+arg.layout_who+") \\new TabStaff \\with {");
	    StringBuilder sb=new StringBuilder();
	    for (int i=track.tuning.length; --i>=0;)
		if (track.tuning[i]>0)
		    sb.append(' ').append(Stuff.midi2ly(track.tuning[i],arg));
	    mfm.print("stringTunings = \\stringTuning <"+sb.substring(1)+">");
	    mfm.unindent("} \\"+lyname);
	}
    }
    @Override final void midi(MusicFileMaker mfm)throws IOException{
	if (arg.midi_who==null)
	    return;
	mfm.indent("\\tag #'("+arg.midi_who+") \\new "+getStaffType()+" = "+Stuff.quote(filename)+" \\with {");
	if (this instanceof DrumTrackFileMaker)
	    mfm.print("drumPitchTable = #(alist->hash-table midiDrumPitches)");
	else{
	    String instrument=Stuff.midiInstrumentToString(track.instrument);
	    if (instrument!=null)
		mfm.print("midiInstrument = #"+Stuff.quote(instrument));
	}
	mfm.unindent("} \\"+lyname);
    }
}
