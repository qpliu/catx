final class TimeSignatureEngraver extends Engraver{
    @Override void engrave(Json measure){
	Json ts=measure.get("signature");
	if (ts!=null){
	    int time_n=ts.get(0).intValue();
	    int time_d=ts.get(1).intValue();
	    if (time_n!=state.time_n || time_d!=state.time_d)
		print("\\time "+time_n+"/"+time_d);
	    state.time_n = time_n;
	    state.time_d = time_d;
	}
    }
}
