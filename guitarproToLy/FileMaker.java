import java.io.*;

class FileMaker{
    final String filename;
    final Main main;
    final PrintStream ps;
    private int indent;
    FileMaker(Main main,String fn)throws IOException{
	this(main,fn,"");
    }
    FileMaker(Main main,String fn,String suffix)throws IOException{
	this.main = main;
	filename = "generated-"+fn;
	ps = new PrintStream(new FileOutputStream(filename+suffix));
    }
    final void noindent(String s){
	ps.println(s);
    }
    final void format(String fmt,Object...va){
	print(String.format(fmt,va));
    }
    final void print(){
	print("");
    }
    final void print(String s){
	for (int i=0; i<indent; i++)
	    ps.print("    ");
	noindent(s);
    }
    final void indent(String s){
	print(s);
	indent++;
    }
    final void unindent(String s){
	--indent;
	print(s);
    }
    final void unindentindent(String s){
	--indent;
	print(s);
	indent++;
    }
    void printInclude(FileMaker fm){
	fm.print("\\include "+Stuff.quote(filename));
    }
}
