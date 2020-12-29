import java.io.*;
import java.util.*;

class TrackFileMaker extends FileMaker{
    final String lyname;
    Arg arg;
    static TrackFileMaker newTrackFileMaker(Main main,int index)throws IOException{
	Arg arg=main.trackarg[index];
	if (arg.partName==null)
	    return null;
	if (false /* check main.gpfile to see if drums */ )
	    return new DrumTrackFileMaker(main,arg,arg.partName);
	if (arg.output_tabs)
	    return new TabsTrackFileMaker(main,arg,arg.partName);
	return new LyTrackFileMaker(main,arg,arg.partName);
    }
    TrackFileMaker(Main main,Arg arg,String name,String suffix)throws IOException{
	super(main,name,suffix);
	this.arg = arg;
	lyname = arg.partName;
    }
    void make()throws IOException{
	indent(lyname+" = {");
	makeMeasures();
	unindent("}");
    }
    final void makeMeasures()throws IOException{
	for (Gpfile.Measure measure:main.gpfile.measures){
	    if (measure.rehearsalMark!=null)
		noindent("\\mymark "+Stuff.escape(measure.rehearsalMark)+" #"+measure.number);
	    makeMeasure(measure);
	}
    }
    void makeMeasure(Gpfile.Measure measure)throws IOException{
    }
/*
    private static final Instrument[]instruments={
	new LyricsInstrument(),
	new DrumsInstrument(),
	new GuitarInstrument(),
    };
    private static Engraver measureNumberEngraver=new MeasureNumberEngraver();
    private static final Engraver[]engravers={
	new MetadataEngraver(),
	measureNumberEngraver,
	new TimeSignatureEngraver(),
	new TempoEngraver(),
	new MarkerEngraver(),
    };
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	Instrument instrument=null;
	for (Instrument i:instruments)
	    if (i.matches(state)){
		instrument = i;
		break;
	    }
	if (instrument==null)
	    throw new IOException("No Instrument matches.");
	for (int pass=0; pass<2; pass++){
	    state.startPass(pass);
	    for (Engraver e:engravers)
		e.setState(state);
	    instrument.setState(state);
	    instrument.printHead();
	    instrument.printTwo();
	    List<Json>measures=state.part.get("measures").list;
	    for (int i=0; i<measures.size(); i++){
		Json measure=measures.get(i);
		for (Engraver e:engravers)
		    e.engrave(measure);
		instrument.engrave(measure);
	    }
	    while (pass==1 && instrument.events.size()!=0){
		measureNumberEngraver.engrave(null);
		instrument.engrave(null);
	    }
	    instrument.printFoot();
	}
*/
}
