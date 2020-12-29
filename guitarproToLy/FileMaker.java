import java.io.*;

abstract class FileMaker{
    final String filename;
    final Main main;
    final PrintStream ps;
    private int indent;
    FileMaker(Main main,String name)throws IOException{
	this(main,name,"");
    }
    FileMaker(Main main,String name,String suffix)throws IOException{
	this.main = main;
	this.filename = "generated-"+name;
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
}
