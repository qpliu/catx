import java.io.*;
import java.util.*;

final class MarkupFileMaker extends FileMaker{
    MarkupFileMaker(Main main)throws IOException{
	super(main,"markup");
    }
    void make()throws IOException{
	indent("markupStuff = {");
	print("\\override Score.RehearsalMark.self-alignment-X = #LEFT");
	unindent("}");
    }
}
