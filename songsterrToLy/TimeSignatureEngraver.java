final class TimeSignatureEngraver extends Engraver{
    @Override void engrave(Json measure,Json nextMeasure){
	Json ts=measure.get("signature");
	if (ts!=null){
	    int timen=ts.get(0).intValue();
	    int timed=ts.get(0).intValue();
	    if (timen!=state.timen || timed!=state.timed)
		print("\\time "+timen+"/"+timed);
	    state.timen = timen;
	    state.timed = timed;
	}
    }
}
