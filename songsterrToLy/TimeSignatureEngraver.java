final class TimeSignatureEngraver extends Engraver{
    @Override void engrave(Json measure){
	Json ts=measure.get("signature");
	if (ts!=null){
	    state.time_n = ts.get(0).intValue();
	    state.time_d = ts.get(1).intValue();
	    print("\\time "+state.time_n+"/"+state.time_d);
	}
    }
}
