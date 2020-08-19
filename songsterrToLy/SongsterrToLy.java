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
	System.err.println("Usage: java SongsterrToLy [options] url");
	System.err.println("url is something like https://www.songsterr.com/a/wsa/momoiro-clover-z-moon-pride-tab-s456232t0 or - for stdin.");
	System.err.println("options:");
	System.err.println("[--drumMap map]  Specify drum map.  Something like --drumMap \"49 cymc 0,38 sn 0,36 bd 1\"");
	System.err.println("[--lyrics]  Extract lyrics instead of notes.");
	System.err.println("[--lyrics-in-voices]  Extract lyrics from voices instead of lyrics.");
	System.err.println("[--name partName]  Specify partName.");
	System.err.println("[--no-ghost-notes]  Don't include ghost notes.");
	System.err.println("[--no-string-numbers]  Don't include string numbers.");
	System.err.println("[--output-text]  Output text.");
	System.err.println("[--output-relative]  Output \\relative notes.");
	System.err.println("[--output-tabs]  Output tabs instead of notes.");
	System.err.println("[--scale scale]  Specify note spelling.  Something like --scale \"c des eisis\"");
	System.err.println("[--shift n/d]  Shift notes right n/d beats.  Use --shift -21/5 to shift notes left 4 1/5th beat.");
	System.exit(1);
    }
    public static void main(String[]argv)throws IOException{
	State state=new State();
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("--lyrics"))
		state.argv_lyrics = true;
	    else if (argv[i].equals("--lyrics-in-voices"))
		state.argv_lyrics_in_voices = true;
	    else if (argv[i].equals("--no-string-numbers"))
		state.argv_no_string_numbers = true;
	    else if (argv[i].equals("--no-ghost-notes"))
		state.argv_no_ghost_notes = true;
	    else if (argv[i].equals("--output-tabs"))
		state.argv_output_tabs = true;
	    else if (argv[i].equals("--output-text"))
		state.argv_output_text = true;
	    else if (argv[i].equals("--output-relative"))
		state.argv_output_relative = true;
	    else if (argv[i].equals("--shift")){
		int j=argv[++i].indexOf('/');
		if (j==-1)
		    state.argv_shift = new Rational(Long.parseLong(argv[i]));
		else
		    state.argv_shift = new Rational(Long.parseLong(argv[i].substring(0,j)),Long.parseLong(argv[i].substring(j+1)));
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
	    state.endRelative(instrument);
	    instrument.printFoot();
	}
    }
}
