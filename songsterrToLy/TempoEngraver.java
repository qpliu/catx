final class TempoEngraver extends Engraver{
    @Override void engrave(Json measure,Json nextMeasure){
	Json index=measure.get("index");
	for (Json t:state.part.get("automations").get("tempo").list)
	    if (t.get("measure").equals(index)){
		int bpm=t.get("bpm").intValue();
		if (bpm!=state.tempo)
		    print("\\tempo "+t.get("type").intValue()+" = "+bpm);
		state.tempo = bpm;
	    }
    }
}
