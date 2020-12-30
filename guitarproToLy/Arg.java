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
    String layout_extra;
    @Override public Arg clone(){
	try{
	    Arg a=(Arg)super.clone();
	    a.scale = a.scale.clone();
	    return a;
	}catch (CloneNotSupportedException e){
	    throw new RuntimeException(e);
	}
    }
}
