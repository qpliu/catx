final class MeasureNumberEngraver extends Engraver{
    @Override void engrave(Json measure){
	state.measureStartTime = state.measureStartTime.add(state.time_n);
	state.measureNumber++;
    }
}
