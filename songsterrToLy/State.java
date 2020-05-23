final class State{
    final String partName;
    final boolean lyrics;
    final Json json;
    final Json meta;
    final Json data;
    final Json part;
    final Json track;
    int measureNumber;
    int indent;
    int tempo;
    int timen,timed;
    State(Json json,boolean lyrics,String partName){
	this.json = json;
	this.lyrics = lyrics;
	this.partName = partName;
	data = json.get("data");
	part = data.get("part");
	meta = json.get("meta");
	track = json.get("track");
    }
}
