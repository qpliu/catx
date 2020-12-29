final class MeasureNumberEngraver extends Engraver{
    @Override void engrave(Json measure){
	state.measureStartTime = state.measureStartTime.add(state.time_n);
	state.measureNumber++;
	if (measure!=null && state.measureNumber!=measure.get("index").intValue())
	    throw new RuntimeException();
    }
}
