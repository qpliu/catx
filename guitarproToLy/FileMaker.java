import java.io.*;

class FileMaker{
    static final String GENERATED_PREFIX="generated-";
    final String original_filename;
    final String filename;
    final Main main;
    final PrintStream ps;
    private int indent;
    FileMaker(Main main,String fn)throws IOException{
	this(main,fn,"");
    }
    FileMaker(Main main,String original_filename,String suffix)throws IOException{
	this.main = main;
	this.original_filename = original_filename;
	if (main.globalarg.modified_filename.contains(original_filename)){
	    filename = original_filename;
	    ps = new PrintStream(OutputStream.nullOutputStream());
	}else{
	    filename = GENERATED_PREFIX+original_filename;
	    ps = new PrintStream(new FileOutputStream(filename+suffix));
	}
    }
    void printMeasureStuff(Gpfile.Measure measure){
	if (measure.rehearsalMark!=null)
	    noindent("\\mymark "+Stuff.quote(measure.rehearsalMark)+" #"+measure.name);
	if (measure.twiddledRepeatStart!=0)
	    indent("\\repeat volta "+measure.twiddledRepeatStart+" {"); // }
	if (measure.twiddledRepeatAlternate==1)
	    /*{*/unindentindent("} \\alternative { {");//}}
	if (measure.twiddledRepeatAlternate>1)
	    /*{*/unindentindent("} {");//}
    }
    void printMeasureStuffEnd(Gpfile.Measure measure){
	if (measure.twiddledRepeatEnd==1)
	    /*{*/unindent("}");
	if (measure.twiddledRepeatEnd>1)
	    /*{{*/unindent("} }");
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
