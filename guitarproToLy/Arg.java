final class Arg implements Cloneable{
    String partName;
    String who;
    String drumMap;
    String[]scale={"c","cis","d","dis","e","f","fis","g","gis","a","ais","b"};
    boolean no_string_numbers;
    boolean output_text;
    boolean output_tabs;
    boolean no_ghost_notes;
    boolean only_lowest_note;
    boolean only_highest_note;
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
