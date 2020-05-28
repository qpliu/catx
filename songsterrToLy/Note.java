interface Note extends Comparable<Note>{
    String getLyNote();
    default String getLySuffix(){
	return "";
    }
    default String tieString(){
	return getLyNote()+getLySuffix();
    }
    default Note tie(Note rhs){
	return this;
    }
    default Note[]split(){
	return new Note[]{this,this};
    }
}
