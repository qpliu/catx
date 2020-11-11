final class MetadataEngraver extends Engraver{
    @Override void engrave(Json measure){
	if (state.measureNumber==0){
	    noindent("% url: "+state.argv_url);
	    noindent("% artist: "+state.meta.get("artist").stringValue());
	    noindent("% title: "+state.meta.get("title").stringValue());
	    noindent("% instrument: "+state.track.get("instrument").stringValue());
	    noindent("% name: "+state.track.get("name").stringValue());
	    if (state.argv_scale!=null)
		noindent("% scale "+state.argv_scale);
	}
    }
}
