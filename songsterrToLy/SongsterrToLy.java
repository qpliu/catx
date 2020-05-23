import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

final class SongsterrToLy{
    private static final Instrument[]instruments={
	new LyricsInstrument(),
	new DrumsInstrument(),
	new GuitarInstrument(),
    };
    private static final Engraver[]engravers={
	new MetadataEngraver(),
	new MeasureNumberEngraver(),
	new TimeSignatureEngraver(),
	new TempoEngraver(),
	new MarkerEngraver(),
    };
    private static void usage(){
	System.err.println("Usage: java SongsterrToLy [--lyrics] [--name partName] url");
	System.err.println("url is something like \"https://www.songsterr.com/a/wsa/momoiro-clover-z-moon-pride-tab-s456232t0\" or \"-\" for stdin");
	System.exit(1);
    }
    public static void main(String[]argv)throws IOException{
	State state=new State();
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("--lyrics"))
		state.lyrics = true;
	    else if (argv[i].equals("--name"))
		state.partName = argv[++i];
	    else if (state.url!=null)
		usage();
	    else
		state.url = argv[i];
	if (state.url==null)
	    usage();
	InputStream is;
	if (state.url.equals("-"))
	    is = System.in;
	else
	    is = new URL(state.url).openConnection().getInputStream();
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	for (int i; (i=is.read())!=-1; baos.write(i));
	Matcher m=Pattern.compile("(?s).*<script id=\"state\" type=\"application/json\">(.*?)</script>.*").matcher(baos.toString());
	if (!m.matches())
	    throw new IOException("State pattern did not match.");
	state.json = Json.parse(m.group(1));
	state.data = state.json.get("data");
	state.part = state.data.get("part");
	state.meta = state.json.get("meta");
	state.track = state.json.get("track");
	Instrument instrument=null;
	for (Instrument i:instruments)
	    if (i.matches(state)){
		instrument = i;
		break;
	    }
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
