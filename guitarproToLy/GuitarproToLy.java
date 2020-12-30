import java.io.*;
import java.util.*;

final class GuitarproToLy{
    private static void usage(){
	System.err.println("Usage: java GuitarproToLy [global options] track n1 [track n1 options] track n2 [track n2 options] ... <gpfile");
	System.err.println("options:");
	System.err.println("[drumMap map]  Specify drum map.  Something like --drumMap \"49 cymc 0,38 sn 0,36 bd 1\"");
	System.err.println("[instrument-name name]  Specify instrument name.");
	System.err.println("[instrument-short-name name]  Specify short instrument name.");
	System.err.println("[layout-extra stuff]  Extra stuff for layout.");
	System.err.println("[layout-tabs]  Layout tabs.");
	System.err.println("[layout-who who]  Specify who for layout.");
	System.err.println("[midi-who who]  Specify who for midi.");
	System.err.println("[music-extra stuff]  Extra stuff for music.");
	System.err.println("[name partName]  Specify partName.");
	System.err.println("[output-chords]  Output chords.");
	System.err.println("[output-karaoke]  Output karaoke.");
	System.err.println("[output-lyrics]  Output lyrics.");
	System.err.println("[output-text]  Output text.");
	System.err.println("[preset who]  Use preset settings.");
	System.err.println("[scale scale]  Specify note spelling.  Something like scale \"c des eisis\"");
	System.err.println("[shift n/d]  Shift notes right n/d beats.  Use shift -21/5 to shift notes left 4 1/5th beat.");
	System.err.println("[string-numbers]  Include string numbers.");
	System.err.println("[verbose level]");
	System.exit(1);
    }
    public static void main(String[]argv)throws IOException{
	List<Arg>trackarg=new ArrayList<Arg>();
	Arg globalarg=new Arg();
	Arg arg=globalarg;
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("track")){
		arg = globalarg.clone();
		arg.trackNumber = Integer.parseInt(argv[++i]);
		trackarg.add(arg);
	    }else if (argv[i].equals("drumMap"))
		arg.drumMap = argv[++i];
	    else if (argv[i].equals("instrument-name"))
		arg.instrument_name = argv[++i];
	    else if (argv[i].equals("instrument-short-name"))
		arg.instrument_short_name = argv[++i];
	    else if (argv[i].equals("layout-extra"))
		arg.layout_extra.add(argv[++i]);
	    else if (argv[i].equals("layout-tabs"))
		arg.layout_tabs = true;
	    else if (argv[i].equals("layout-who"))
		arg.layout_who = argv[++i];
	    else if (argv[i].equals("midi-who"))
		arg.midi_who = argv[++i];
	    else if (argv[i].equals("music-extra"))
		arg.music_extra.add(argv[++i]);
	    else if (argv[i].equals("name"))
		arg.partName = argv[++i];
	    else if (argv[i].equals("string-numbers"))
		arg.string_numbers = true;
	    else if (argv[i].equals("output-chords"))
		arg.output_chords = true;
	    else if (argv[i].equals("output-karaoke"))
		arg.output_karaoke = true;
	    else if (argv[i].equals("output-lyrics"))
		arg.output_lyrics = true;
	    else if (argv[i].equals("output-text"))
		arg.output_text = true;
	    else if (argv[i].equals("preset"))
		arg.setPreset(argv[++i]);
	    else if (argv[i].equals("scale"))
		arg.setScale(argv[++i]);
	    else if (argv[i].equals("shift")){
		int j=argv[++i].indexOf('/');
		if (j==-1)
		    arg.shift = new Rational(Long.parseLong(argv[i]));
		else
		    arg.shift = new Rational(Long.parseLong(argv[i].substring(0,j)),Long.parseLong(argv[i].substring(j+1)));
	    }else if (argv[i].equals("verbose"))
		Log.level = Integer.parseInt(argv[++i]);
	    else
		usage();
	Gpfile gpfile=new Gp5file(new DataInputStream(System.in),globalarg);
	new Main(gpfile,globalarg,trackarg).make();
    }
}
