final class Arg implements Cloneable{
    int trackNumber=-1;
    String partName;
    String who;
    String drumMap;
    String[]scale={"c","cis","d","dis","e","f","fis","g","gis","a","ais","b"};
    boolean output_chords;
    boolean output_karaoke;
    boolean output_lyrics;
    boolean output_tabs;
    boolean output_text;
    boolean no_ghost_notes;
    boolean no_string_numbers;
    Rational shift=Rational.ZERO;
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
