final class State{
    String argv_partName="partName";
    String argv_drumMap=null;
    String argv_url;
    boolean argv_lyrics;
    boolean argv_no_string_numbers;
    boolean argv_omit_ghost_notes;
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
    Rational measureStartTime;
    void startPass(int pass){
	this.pass = pass;
	measureNumber = 0;
	indent = 0;
	tempo = 0;
	time_n = 0;
	time_d = 0;
	measureStartTime = Rational.ZERO;
    }
}
