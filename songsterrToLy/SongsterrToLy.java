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
    private static Engraver measureNumberEngraver=new MeasureNumberEngraver();
    private static final Engraver[]engravers={
	new MetadataEngraver(),
	measureNumberEngraver,
	new TimeSignatureEngraver(),
	new TempoEngraver(),
	new MarkerEngraver(),
    };
    private static void usage(){
	System.err.println("Usage: java SongsterrToLy [--lyrics] [--name partName] [--drumMap map] [--scale scale] [--shift n/d] url");
	System.err.println("url is something like \"https://www.songsterr.com/a/wsa/momoiro-clover-z-moon-pride-tab-s456232t0\" or \"-\" for stdin");
	System.exit(1);
    }
    public static void main(String[]argv)throws IOException{
	State state=new State();
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("--lyrics"))
		state.argv_lyrics = true;
	    else if (argv[i].equals("--shift")){
		int j=argv[++i].indexOf('/');
		int shift_n;
		int shift_d=1;
		if (j==-1)
		    shift_n = Integer.parseInt(argv[i]);
		else{
		    shift_n = Integer.parseInt(argv[i].substring(0,j));
		    shift_d = Integer.parseInt(argv[i].substring(j+1));
		}
		state.argv_shift = Instrument.DIVISION*shift_n/shift_d;
	    }else if (argv[i].equals("--name"))
		state.argv_partName = argv[++i];
	    else if (argv[i].equals("--scale"))
		Stuff.setScale(argv[++i]);
	    else if (argv[i].equals("--drumMap"))
		state.argv_drumMap = argv[++i];
	    else if (state.argv_url!=null)
		usage();
	    else
		state.argv_url = argv[i];
	if (state.argv_url==null)
	    usage();
	InputStream is;
	if (state.argv_url.equals("-"))
	    is = System.in;
	else
	    is = new URL(state.argv_url).openConnection().getInputStream();
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
	for (int pass=0; pass<2; pass++){
	    state.startPass(pass);
	    for (Engraver e:engravers)
		e.setState(state);
	    instrument.setState(state);
	    instrument.printHead();
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
    }
}
