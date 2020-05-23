final class MeasureNumberEngraver extends Engraver{
    @Override void engrave(Json measure,Json nextMeasure){
	int measureNumber=measure.get("index").intValue();
	if (measureNumber!=++state.measureNumber)
	    print("\\set Score.currentBarNumber = #"+measureNumber);
    }
}
