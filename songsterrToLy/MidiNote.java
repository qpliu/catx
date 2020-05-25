class MidiNote implements Note{
    final int note;
    MidiNote(int note){
	this.note = note;
    }
    public int compareTo(Note n){
	return Integer.compare(note,((MidiNote)n).note);
    }
    @Override public String getLyNote(){
	return Stuff.midi2ly(note);
    }
}
