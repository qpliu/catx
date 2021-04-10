import java.io.*;
import java.util.*;

final class Arg implements Serializable{
    int trackNumber=-1;
    String name;
    String layout_who;
    String midi_who;
    int midi_instrument=-1;
    String drumMap;
    final String[]scale={"c","cis","d","dis","e","f","fis","g","gis","a","ais","b"};
    boolean layout_tabs;
    boolean output_chords;
    boolean use_bend_end;
    boolean use_bend_start;
    boolean output_dotext;
    boolean output_drums;
    boolean output_karaoke;
    boolean output_lilypond;
    boolean output_lyrics;
    boolean output_vocaloid;
    boolean no_chords;
    boolean no_ghost_notes;
    boolean string_numbers;
    Rational shift=Rational.ZERO;
    final Map<String,String>karaoke_lyrics_parameter=new HashMap<String,String>();
    String prefix="";
    String instrument_name;
    String instrument_short_name;
    String which_lyrics="";
    int transpose;
    final List<Gpfile.TrackMeasureLyrics>add_lyrics=new ArrayList<Gpfile.TrackMeasureLyrics>();
    final List<String>layout_extra=new ArrayList<String>();
    final Map<Integer,List<String>>music_extra=new HashMap<Integer,List<String>>();
    final Map<Integer,List<String>>markup_extra=new HashMap<Integer,List<String>>();
    final Set<String>modified_filename=new HashSet<String>();
    void setScale(String s){
	for (StringTokenizer st=new StringTokenizer(s); st.hasMoreTokens();){
	    String ly=st.nextToken();
	    scale[Stuff.ly2midi(ly)%12] = ly;
	}
    }
    void setPreset(String preset){
	if (preset.equals("flats"))
	    setScale("des ees ges aes bes");
	else if (preset.equals("gg")){
	    name = "drs";
	    layout_who = "allPart ggPart kavPart";
	    midi_who = "midiGg";
	    instrument_name = "Drums";
	    instrument_short_name = "Drs";
	}else if (preset.equals("vocaloid")){
	    name = "vocaloid";
	    midi_who = "midiVocaloid";
	    no_chords = true;
	}else if (preset.equals("kav")){
	    name = "vocals";
	    layout_who = "allPart kavPart";
	    midi_instrument = 54;
	    midi_who = "midiKaraoke";
	    instrument_name = "Vocals";
	    instrument_short_name = "Vo";
	    Stuff.add(music_extra,0,"\\myVocalsStuff");
	}else if (preset.equals("susan")){
	    name = "steelpan";
	    layout_who = "allPart susanPart";
	    midi_who = "midiSusan";
	    instrument_name = "Steelpan";
	    instrument_short_name = "Sp";
	    Stuff.add(music_extra,0,"\\myVocalsStuff");
	    setScale("gis fis cis");
	    no_chords = true;
	}else if (preset.equals("midiMuted") || preset.equals("midiOne") || preset.equals("midiTwo")){
	    name = preset;
	    midi_who = preset;
	}else if (preset.equals("bass")){
	    name = "bass";
	    layout_who = "allPart bassPart";
	    layout_extra.add("\\clef \"bass_8\"");
	    layout_tabs = true;
	    midi_who = "midiBass";
	    instrument_name = "Bass";
	    instrument_short_name = "Ba";
	    string_numbers = true;
	}else if (preset.equals("peter")){
	    name = "guitar";
	    layout_who = "allPart peterPart";
	    layout_extra.add("\\clef \"treble_8\"");
	    layout_tabs = true;
	    midi_who = "midiPeter";
	    instrument_name = "Guitar";
	    instrument_short_name = "Gtr";
	    string_numbers = true;
	}else
	    throw new RuntimeException("Bad preset");
    }
    private static void usage(){
	System.err.println("Usage: java GuitarproToLy [global options] track n1 [track n1 options] track n2 [track n2 options] ... <gpfile");
	System.err.println("options:");
	System.err.println("[drumMap map]  Specify drum map.  Something like --drumMap \"49 cymc 0,38 sn 0,36 bd 1\"");
	System.err.println("[add-lyrics track measure lyrics]  Add lyrics.");
	System.err.println("[instrument-name name]  Specify instrument name.");
	System.err.println("[instrument-short-name name]  Specify short instrument name.");
	System.err.println("[karaoke-lyrics-parameter key value]  Specify parameters for generating karaoke lyrics.");
	System.err.println("[layout-extra stuff]  Extra stuff for layout.");
	System.err.println("[layout-tabs]  Layout tabs.");
	System.err.println("[layout-who who]  Specify who for layout.");
	System.err.println("[markup-extra measure stuff]  Extra stuff for markup.");
	System.err.println("[midi-instrument number]  Specify midi instrument.");
	System.err.println("[midi-who who]  Specify who for midi.");
	System.err.println("[modified-filename filename]  Don't generate generated-filename.");
	System.err.println("[music-extra measure stuff]  Extra stuff for music.");
	System.err.println("[name name]  Specify name.");
	System.err.println("[no-chords true|false]  Get rid of chords.");
	System.err.println("[no-ghost-notes true|false]  Get rid of ghost notes.");
	System.err.println("[output-chords]  Output chords.");
	System.err.println("[output-dotext]  Output dotext.");
	System.err.println("[output-drums]  Output drums.");
	System.err.println("[output-karaoke]  Output karaoke.");
	System.err.println("[output-lilypond]  Output lilypond.");
	System.err.println("[output-lyrics]  Output lyrics.");
	System.err.println("[output-vocaloid]  Output vocaloid.");
	System.err.println("[prefix prefix]  Prepend prefix to output filenames.");
	System.err.println("[preset who]  Use preset settings.");
	System.err.println("[scale scale]  Specify note spelling.  Something like scale \"c des eisis\"");
	System.err.println("[shift n/d]  Shift notes right n/d beats.  Use shift -21/5 to shift notes left 4 1/5th beat.");
	System.err.println("[string-numbers true|false]  Include string numbers.");
	System.err.println("[transpose half_steps]  Specify transpose.  Something like transpose 1");
	System.err.println("[use-bend-end true|false] Replace bent notes with end of note]");
	System.err.println("[use-bend-start true|false] Replace bent notes with start of note]");
	System.err.println("[verbose level]");
	System.err.println("[which-lyrics which]  Choose which lyrics tracks to use--something like \"text,0,1,4\"");
	System.exit(1);
    }
    static Arg getArgs(List<Arg>trackargs,String[]argv)throws Exception{
	Arg globalarg=new Arg();
	Arg arg=globalarg;
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("track")){
		arg = (Arg)Stuff.deserializeserialize(globalarg);
		arg.trackNumber = Integer.parseInt(argv[++i]);
		trackargs.add(arg);
	    }else if (argv[i].equals("drumMap"))
		arg.drumMap = argv[++i];
	    else if (argv[i].equals("add-lyrics")){
		Gpfile.TrackMeasureLyrics tml=new Gpfile.TrackMeasureLyrics();
		tml.which = argv[++i];
		tml.track = Integer.parseInt(argv[++i]);
		tml.startingMeasure = Integer.parseInt(argv[++i]);
		tml.lyrics = argv[++i];
		arg.add_lyrics.add(tml);
	    }else if (argv[i].equals("instrument-name"))
		arg.instrument_name = argv[++i];
	    else if (argv[i].equals("instrument-short-name"))
		arg.instrument_short_name = argv[++i];
	    else if (argv[i].equals("karaoke-lyrics-parameter"))
		arg.karaoke_lyrics_parameter.put(argv[++i],argv[++i]);
	    else if (argv[i].equals("layout-extra"))
		arg.layout_extra.add(argv[++i]);
	    else if (argv[i].equals("layout-tabs"))
		arg.layout_tabs = true;
	    else if (argv[i].equals("layout-who"))
		arg.layout_who = argv[++i];
	    else if (argv[i].equals("markup-extra"))
		Stuff.add(arg.markup_extra,Math.max(Integer.parseInt(argv[++i])-1,0),argv[++i]);
	    else if (argv[i].equals("midi-instrument"))
		arg.midi_instrument = Integer.parseInt(argv[++i]);
	    else if (argv[i].equals("midi-who"))
		arg.midi_who = argv[++i];
	    else if (argv[i].equals("modified-filename"))
		arg.modified_filename.add(argv[++i]);
	    else if (argv[i].equals("music-extra"))
		Stuff.add(arg.music_extra,Math.max(Integer.parseInt(argv[++i])-1,0),argv[++i]);
	    else if (argv[i].equals("name"))
		arg.name = argv[++i];
	    else if (argv[i].equals("no-chords"))
		arg.no_chords = Boolean.valueOf(argv[++i]);
	    else if (argv[i].equals("no-ghost-notes"))
		arg.no_ghost_notes = Boolean.valueOf(argv[++i]);
	    else if (argv[i].equals("output-chords"))
		arg.output_chords = true;
	    else if (argv[i].equals("output-dotext"))
		arg.output_dotext = true;
	    else if (argv[i].equals("output-drums"))
		arg.output_drums = true;
	    else if (argv[i].equals("output-karaoke"))
		arg.output_karaoke = true;
	    else if (argv[i].equals("output-lilypond"))
		arg.output_lilypond = true;
	    else if (argv[i].equals("output-lyrics"))
		arg.output_lyrics = true;
	    else if (argv[i].equals("output-vocaloid"))
		arg.output_vocaloid = true;
	    else if (argv[i].equals("prefix"))
		arg.prefix = argv[++i];
	    else if (argv[i].equals("preset"))
		arg.setPreset(argv[++i]);
	    else if (argv[i].equals("scale"))
		arg.setScale(argv[++i]);
	    else if (argv[i].equals("shift"))
		arg.shift = Rational.parseRational(argv[++i]);
	    else if (argv[i].equals("string-numbers"))
		arg.string_numbers = Boolean.valueOf(argv[++i]);
	    else if (argv[i].equals("transpose"))
		arg.transpose = Integer.parseInt(argv[++i]);
	    else if (argv[i].equals("use-bend-end"))
		arg.use_bend_end = Boolean.valueOf(argv[++i]);
	    else if (argv[i].equals("use-bend-start"))
		arg.use_bend_start = Boolean.valueOf(argv[++i]);
	    else if (argv[i].equals("verbose"))
		Log.level = Integer.parseInt(argv[++i]);
	    else if (argv[i].equals("which-lyrics"))
		arg.which_lyrics = argv[++i];
	    else{
		Log.error("%s is weird",argv[i]);
		usage();
	    }
	Set<String>usedNames=new HashSet<String>();
	Set<String>usedInstrumentNames=new HashSet<String>();
	for (Arg a:trackargs){
	    if (!usedNames.add(a.name))
		for (char c='A';; c++)
		    if (usedNames.add(a.name+c)){
			a.name += c;
			break;
		    }else if (c=='Z')
			throw new RuntimeException("Too many "+a.name);
	    if (a.instrument_name!=null && !usedInstrumentNames.add(a.instrument_name))
		for (int i=2;; i++)
		    if (usedInstrumentNames.add(a.instrument_name+i)){
			a.instrument_name += i;
			if (a.instrument_short_name!=null)
			    a.instrument_short_name += i;
			break;
		    }
	}
	return globalarg;
    }
}
