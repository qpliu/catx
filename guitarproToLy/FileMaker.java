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
	if (measure.repeatStart){
	    int i;
	    for (i=measure.index; main.gpfile.measures[i].repeatEnd==0; i++);
	    indent("\\repeat volta "+main.gpfile.measures[i].repeatEnd+" {"); // }
	}
	if (measure.repeatAlternate>0){
	    for (int i=measure.index-1;;--i)
		if (main.gpfile.measures[i].repeatStart){
		    /*{*/unindentindent("} \\alternative { {");//}}
		    break;
		}else if (main.gpfile.measures[i].repeatEnd>0)
		    break;
		else if (main.gpfile.measures[i].repeatAlternate>0){
		    /*{*/unindentindent("} {");//}
		    break;
		}
	}
    }
    void printMeasureStuffEnd(Gpfile.Measure measure){
	if (measure.repeatAlternate>0 && main.gpfile.measures[measure.index-1].repeatEnd>0)
	    /*{{*/unindent("} }");
	if (measure.repeatEnd>0)
	    if (main.gpfile.measures[measure.index+1].repeatAlternate>0)
		/*{*/unindentindent("} {");//}
	    else
		/*{*/unindent("}");
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
