class TextNote implements Note{
    final String text;
    TextNote(String text){
	this.text = text;
    }
    public int compareTo(Note n){
	return text.compareTo(((TextNote)n).text);
    }
    @Override public String getLyNote(){
	return '`'+text+'`';
    }
}
