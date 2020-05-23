final class State{
    String argv_partName="partName";
    String argv_drumMap=null;
    String argv_url;
    boolean argv_lyrics;
    int argv_shift;
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
    int measureStartTime;
    void startPass(int pass){
	this.pass = pass;
	measureNumber = 0;
	indent = 0;
	tempo = 0;
	time_n = 0;
	time_d = 0;
	measureStartTime = 0;
    }
}
