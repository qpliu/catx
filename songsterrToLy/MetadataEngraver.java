final class MetadataEngraver extends Engraver{
    private boolean done;
    @Override void engrave(Json measure,Json nextMeasure){
	if (done)
	    return;
	done = true;
	noindent("% url: "+state.url);
	noindent("% artist: "+state.meta.get("artist").stringValue());
	noindent("% title: "+state.meta.get("title").stringValue());
	noindent("% instrument: "+state.track.get("instrument").stringValue());
	noindent("% name: "+state.track.get("name").stringValue());
    }
}
