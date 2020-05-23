final class State{
    final Json json;
    final Json meta;
    final Json data;
    final Json part;
    final Json track;
    int measureNumber;
    int indent;
    int tempo;
    int timen,timed;
    State(Json json){
	this.json = json;
	data = json.get("data");
	part = data.get("part");
	meta = json.get("meta");
	track = json.get("track");
    }
}
