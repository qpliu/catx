import java.util.*;

final class Arg implements Cloneable{
    int trackNumber=-1;
    String name;
    String layout_who;
    String midi_who;
    String drumMap;
    String[]scale={"c","cis","d","dis","e","f","fis","g","gis","a","ais","b"};
    boolean generate_lyrics;
    boolean layout_tabs;
    boolean output_chords;
    boolean output_dotext;
    boolean output_drums;
    boolean output_karaoke;
    boolean output_lilypond;
    boolean output_lyrics;
    boolean string_numbers;
    Rational shift=Rational.ZERO;
    String instrument_name;
    String instrument_short_name;
    String which_lyrics="";
    String transpose="";
    List<String>layout_extra=new ArrayList<String>();
    List<String>music_extra=new ArrayList<String>();
    List<String>markup_extra=new ArrayList<String>();
    Set<String>modified_filename=new HashSet<String>();
    @Override public Arg clone(){
	try{
	    Arg a=(Arg)super.clone();
	    a.scale = a.scale.clone();
	    a.layout_extra = new ArrayList<String>(a.layout_extra);
	    a.music_extra = new ArrayList<String>(a.music_extra);
	    a.modified_filename = new HashSet<String>(a.modified_filename);
	    return a;
	}catch (CloneNotSupportedException e){
	    throw new RuntimeException(e);
	}
    }
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
	}else if (preset.equals("kav")){
	    name = "vocals";
	    layout_who = "allPart kavPart";
	    midi_who = "midiKaraoke";
	    instrument_name = "Vocals";
	    instrument_short_name = "Vo";
	    music_extra.add("\\myVocalsStuff");
	}else if (preset.equals("susan")){
	    name = "steelpan";
	    layout_who = "allPart susanPart";
	    midi_who = "midiSusan";
	    instrument_name = "Steelpan";
	    instrument_short_name = "Sp";
	    music_extra.add("\\myVocalsStuff");
	    setScale("gis fis cis");
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
	    instrument_short_name  = "Ba";
	    string_numbers  = true;
	}else if (preset.equals("peter")){
	    name = "guitar";
	    layout_who = "allPart peterPart";
	    layout_extra.add("\\clef \"treble_8\"");
	    layout_tabs = true;
	    midi_who = "midiPeter";
	    instrument_name = "Guitar";
	    instrument_short_name  = "Gtr";
	    string_numbers  = true;
	}else
	    throw new RuntimeException();
    }
}
