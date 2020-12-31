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
    boolean output_dotext;
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
	    partName = "drs";
	    layout_who = "allPart ggPart";
	    instrument_name = "Drums";
	    instrument_short_name = "Drs";
	    midi_who = "midiGg";
	}else if (preset.equals("kav")){
	    partName = "vocals";
	    layout_who = "allPart kavPart";
	    instrument_name = "Vocals";
	    instrument_short_name = "Vo";
	    midi_who = "midiKaraoke";
	    layout_extra.add("\\vocalsLyrics");
	    layout_extra.add("\\drs");
	    music_extra.add("\\myVocalsStuff");
	}else if (preset.equals("susan")){
	    setScale("gis fis cis");
	    partName = "steelpan";
	    layout_who = "allPart susanPart";
	    instrument_name = "Steelpan";
	    instrument_short_name = "Sp";
	    midi_who = "midiSusan";
	    music_extra.add("\\myVocalsStuff");
	}else if (preset.equals("kav")){
	    partName = "vocals";
	    layout_who = "allPart kavPart";
	    instrument_name = "Vocals";
	    instrument_short_name = "Vo";
	    midi_who = "midiKav";
	}else if (preset.equals("karaoke")){
	    partName = "karaoke";
	    output_karaoke = true;
	    midi_who = "midiKaraoke";
	}else if (preset.equals("midiOne") || preset.equals("midiTwo") || preset.equals("midiThree") || preset.equals("midiFour")){
	    partName = preset;
	    midi_who = preset;
	}else if (preset.equals("lyrics")){
	    partName = "vocalsLyrics";
	    output_lyrics = true;
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
