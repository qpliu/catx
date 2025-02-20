import java.io.*;
import java.util.*;

final class MarkupFileMaker extends FileMaker{
    static final String lyname="markupStuff";
    MarkupFileMaker(Main main)throws IOException{
	super(main,"markup");
    }
    void make()throws IOException{
	indent(lyname+" = {");
	print("\\override Score.RehearsalMark.self-alignment-X = #LEFT");
	int time_n=4,time_d=4;
	Gpfile.KeySignature key=new Gpfile.KeySignature(0,0);
	int tempo=0;
	boolean tripletFeel=false;
	for (Gpfile.Measure measure:main.gpfile.measures){
	    for (String s:main.globalarg.markup_extra.getOrDefault(measure.index,Collections.emptyList()))
		print(s);
	    if (measure.tempo!=tempo){
		tempo = measure.tempo;
		print("\\tempo 4 = "+tempo);
	    }
	    if (!measure.key.equals(key)){
		key = measure.key;
		print(Stuff.keyToLy(main.globalarg,key.key0,key.key1));
	    }
	    if (measure.time_n!=time_n || measure.time_d!=time_d){
		time_n = measure.time_n;
		time_d = measure.time_d;
		print("\\time "+time_n+'/'+time_d);
	    }
	    printMeasureStuff(measure);
	    String swing="";
	    if (measure.tripletFeel!=null!=tripletFeel){
		tripletFeel = measure.tripletFeel!=null;
		if (tripletFeel)
		    swing = "^\\markup { \bold \"Swing\" }";
		else
		    swing = "^\\markup { \bold \"No Swing\" }";
	    }
	    final String suffix=swing;
	    MeasureMaker mm=new MeasureMaker(measure);
	    mm.make(measure.time.add(measure.time_n),new MeasureMaker.GetWhatSuffix(){
		@Override public String getWhat(boolean is_lhs,boolean is_rhs){
		    return "s";
		}
		@Override public String getSuffix(boolean is_lhs,boolean is_rhs){
		    return is_lhs?suffix:"";
		}
	    });
	    print(mm.tail());
	    printMeasureStuffEnd(measure);
	}
	unindent("}");
    }
}
