import java.io.*;
import java.util.*;

final class TrackFileMaker extends FileMaker{
    Arg arg;
    TrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,arg.partName);
	this.arg = arg;
    }
    void make()throws IOException{
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
