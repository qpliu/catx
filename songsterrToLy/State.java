final class State{
    final Json json;
    final Json data;
    final Json part;
    int measureNumber;
    int indent;
    int tempo;
    int timen,timed;
    State(Json json){
	this.json = json;
	data = json.get("data");
	part = data.get("part");
    }
}
