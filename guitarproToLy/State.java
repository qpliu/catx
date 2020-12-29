final class State{
    String argv_partName="partName";
    String argv_drumMap=null;
    String argv_url;
    String argv_scale;
    boolean argv_lyrics;
    boolean argv_lyrics_in_voices;
    boolean argv_no_string_numbers;
    boolean argv_output_relative;
    boolean argv_output_text;
    boolean argv_output_tabs;
    boolean argv_no_ghost_notes;
    boolean argv_only_lowest_note;
    boolean argv_only_highest_note;
    int argv_ottava;
    Rational argv_shift=Rational.ZERO;
    Json json;
    Json meta;
    Json data;
    Json part;
    Json track;
    int pass;
    int measureNumber;
    int indent;
    int tempo;
    int time_n,time_d;
    boolean isRelative;
    boolean last_hp;
    int lastRelative;
    Rational measureStartTime;
    int old_ottava;
    void startPass(int pass){
	this.pass = pass;
	measureNumber = 0;
	indent = 0;
	tempo = 0;
	time_n = 0;
	time_d = 0;
	measureStartTime = Rational.ZERO;
    }
    void startRelative(Engraver engraver){
	if (!isRelative && argv_output_relative){
	    isRelative = true;
	    lastRelative = 0;
	    engraver.indent("\\relative {");
	}
    }
    void endRelative(Engraver engraver){
	if (isRelative){
	    engraver.unindent("}");
	    isRelative = false;
	}
    }
}
