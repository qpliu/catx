final class MarkerEngraver extends Engraver{
    @Override void engrave(Json measure){
	Json m=measure.get("marker");
	if (m!=null){
	    if (!state.argv_output_text)
		noindent("\\mymark \""+m.get("text").stringValue()+"\" #"+state.measureNumber);
	}
    }
}
