final class MeasureNumberEngraver extends Engraver{
    @Override void engrave(Json measure){
	state.measureStartTime += state.time_n*Instrument.DIVISION;
	state.measureNumber++;
    }
}
