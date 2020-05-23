abstract class Engraver{
    State state;
    void setState(State state){
	this.state = state;
    }
    final void noindent(String s){
	if (state.pass==1)
	    System.out.println(s);
    }
    final void print(String s){
	if (state.pass==1)
	    for (int i=0; i<state.indent; i++)
		System.out.print("    ");
	noindent(s);
    }
    final void indent(String s){
	print(s);
	state.indent++;
    }
    final void unindent(String s){
	--state.indent;
	print(s);
    }
    abstract void engrave(Json measure);
}
