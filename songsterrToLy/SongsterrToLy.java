import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

final class SongsterrToLy{
    private static final Instrument[]instruments={
	new DrumsInstrument(),
	new GuitarInstrument(),
    };
    private static final Engraver[]engravers={
	new MeasureNumberEngraver(),
	new TempoEngraver(),
	new TimeSignatureEngraver(),
	new MarkerEngraver(),
    };
    public static void main(String[]argv)throws IOException{
	if (argv.length!=1){
	    System.err.println("Usage: java SongsterrToLy url");
	    System.err.println("    or java SongsterrToLy - <filename");
	    System.exit(1);
	}
	InputStream is;
	if (argv[0].equals("-"))
	    is = System.in;
	else
	    is = new URL(argv[0]).openConnection().getInputStream();
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	for (int i; (i=is.read())!=-1; baos.write(i));
	Matcher m=Pattern.compile("(?s).*<script id=\"state\" type=\"application/json\">(.*?)</script>.*").matcher(baos.toString());
	if (!m.matches())
	    throw new IOException("State pattern did not match.");
	State state=new State(Json.parse(m.group(1)));
	System.out.println("% artist: "+state.meta.get("artist").stringValue());
	System.out.println("% title: "+state.meta.get("title").stringValue());
	Instrument instrument=null;
	for (Instrument i:instruments)
	    if (i.matches(state))
		instrument = i;
	if (instrument==null)
	    throw new IOException("No Instrument matches.");
	for (Engraver e:engravers)
	    e.setState(state);
	instrument.setState(state);
	instrument.printHead();
	List<Json>measures=state.part.get("measures").list;
	for (int i=0; i<measures.size(); i++){
	    Json measure=measures.get(i);
	    Json nextMeasure=i+1<measures.size()?measures.get(i+1):null;
	    for (Engraver e:engravers)
		e.engrave(measure,nextMeasure);
	    instrument.engrave(measure,nextMeasure);
	}
	instrument.printFoot();
    }
}
