final class MarkerEngraver extends Engraver{
    @Override void engrave(Json measure){
	Json m=measure.get("marker");
	if (m!=null){
	    state.endRelative(this);
	    noindent("\\mymark \""+m.get("text").stringValue()+"\" #"+state.measureNumber);
	}
    }
}
