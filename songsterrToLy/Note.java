interface Note extends Comparable<Note>{
    String getLyNote();
    default String getLySuffix(){
	return "";
    }
    default String tieString(){
	return getLyNote()+getLySuffix();
    }
}
