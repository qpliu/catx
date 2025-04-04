import java.io.*;
import java.util.*;

abstract class SuperTrackFileMaker extends ChoppedTrackFileMaker{
    SuperTrackFileMaker(Main main,String suffix,Arg arg)throws IOException{
	super(main,arg,arg.name,suffix,arg.name);
    }
    @Override boolean filterEvents(Gpfile.Event event){
	return event instanceof Gpfile.NoteEvent && !(arg.no_ghost_notes && ((Gpfile.NoteEvent)event).is_ghost);
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
	if (arg.layout_tabs){
	    StringBuilder sb=new StringBuilder();
	    for (int i=track.tuning.length; --i>=0;)
		if (track.tuning[i]>0)
		    sb.append(' ').append(Stuff.midi2ly(track.tuning[i],arg).replace(",","").replace("'",""));
	    if (track.capo!=0)
		sb.append(" capo fret ").append(track.capo);
	    if (sb.length()!=0)
		mfm.print("{ s128_\""+sb.substring(1)+"\" }");
	}
	mfm.unindent(">>");
	if (arg.layout_tabs){
	    mfm.indent("\\tag #'("+arg.layout_who+") \\new TabStaff \\with {");
	    StringBuilder sb=new StringBuilder();
	    for (int i=track.tuning.length; --i>=0;)
		if (track.tuning[i]>0)
		    sb.append(' ').append(Stuff.midi2ly(track.tuning[i]+track.capo,arg));
	    mfm.print("stringTunings = \\stringTuning <"+sb.substring(1)+">");
	    mfm.unindent("} \\"+lyname);
	}
    }
    @Override final void midi(MusicFileMaker mfm)throws IOException{
	if (arg.midi_who==null)
	    return;
	String fn=main.globalarg.modified_filename.contains(original_filename+DotextFileMaker.FILENAME_SUFFIX)?original_filename:GENERATED_PREFIX+original_filename;
	mfm.indent("\\tag #'("+arg.midi_who+") \\new "+getStaffType()+" = "+Stuff.quote(fn)+" \\with {");
	if (this instanceof DrumTrackFileMaker)
	    mfm.print("drumPitchTable = #(alist->hash-table midiDrumPitches)");
	else{
	    String instrument=Stuff.midiInstrumentToString(arg.midi_instrument);
	    if (instrument!=null)
		mfm.print("midiInstrument = #"+Stuff.quote(instrument));
	}
	mfm.unindent("} \\"+lyname);
    }
    @Override MeasureMaker.GetWhatSuffix getRestGetWhatSuffix(){
	return MeasureMaker.REST;
    }
}
