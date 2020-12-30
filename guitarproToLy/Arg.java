import java.util.*;

final class Arg implements Cloneable{
    int trackNumber=-1;
    String partName;
    String layout_who;
    String midi_who;
    String drumMap;
    String[]scale={"c","cis","d","dis","e","f","fis","g","gis","a","ais","b"};
    boolean layout_tabs;
    boolean output_chords;
    boolean output_karaoke;
    boolean output_lyrics;
    boolean output_text;
    boolean no_ghost_notes;
    boolean string_numbers;
    Rational shift=Rational.ZERO;
    String instrument_name;
    String instrument_short_name;
    ArrayList<String>layout_extra=new ArrayList<String>();
    ArrayList<String>music_extra=new ArrayList<String>();
    @Override public Arg clone(){
	try{
	    Arg a=(Arg)super.clone();
	    a.scale = a.scale.clone();
	    a.layout_extra = new ArrayList<String>(a.layout_extra);
	    a.music_extra = new ArrayList<String>(a.music_extra);
	    return a;
	}catch (CloneNotSupportedException e){
	    throw new RuntimeException(e);
	}
    }
    void setPreset(String preset){
	if (preset.equals("gg")){
	    partName = "drs";
	    layout_who = "allPart ggPart";
	    instrument_name = "Drums";
	    instrument_short_name = "Drs";
	    midi_who = "midiGg";
	}else if (preset.equals("bass")){
	    partName = "bass";
	    layout_who = "allPart bassPart";
	    layout_extra.add("\\clef \"bass_8\"");
	    instrument_name = "Bass";
	    instrument_short_name  = "Ba";
	    midi_who = "midiBass";
	    layout_tabs = true;
	    string_numbers  = true;
	}else if (preset.equals("peter")){
	    partName = "guitar";
	    layout_who = "allPart peterPart";
	    layout_extra.add("\\clef \"treble_8\"");
	    instrument_name = "Guitar";
	    instrument_short_name  = "Gtr";
	    midi_who = "midiPeter";
	    layout_tabs = true;
	    string_numbers  = true;
	}else
	    throw new RuntimeException();
    }
}
