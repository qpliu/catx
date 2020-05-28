class MidiNote implements Note{
    final State state;
    final int note;
    MidiNote(State state,int note){
	this.state = state;
	this.note = note;
    }
    public int compareTo(Note n){
	return Integer.compare(note,((MidiNote)n).note);
    }
    @Override public String getLyNote(){
	return Stuff.midi2ly(note,state);
    }
}
