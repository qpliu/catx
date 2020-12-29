final class TempoEngraver extends Engraver{
    @Override void engrave(Json measure){
	Json index=measure.get("index");
	for (Json t:state.part.get("automations").get("tempo").list)
	    if (t.get("measure").equals(index)){
		state.tempo = t.get("bpm").intValue();
		if (!state.argv_output_text)
		    print("\\tempo "+t.get("type").intValue()+" = "+state.tempo);
	    }
    }
}
