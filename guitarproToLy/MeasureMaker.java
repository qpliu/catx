import java.math.*;
import java.util.*;

final class MeasureMaker{
    final Gpfile.Measure measure;
    Rational time;
    private final StringBuilder sb=new StringBuilder();
    private final DurationMaker dm;
    MeasureMaker(Gpfile.Measure measure){
	this.measure = measure;
	time = measure.time;
	dm = new DurationMaker(measure.time_d);
    }
    void make(Rational t,String what,String suffix,String suffix2,boolean skip){
	dm.addDuration(t.subtract(time));
	time = t;
	while (dm.duration.signum()!=0){
	    String[]ba=dm.subtractDuration(skip);
	    sb.append(ba[0]).append(what).append(ba[1]).append(suffix).append(' ');
	    suffix = suffix2;
	}
    }
    void skip(Rational t){
	make(t,"\\skip","","",true);
    }
    void rest(Rational t){
	make(t,"r","","",false);
    }
    String tail(){
	return sb.append(dm.tail()).append('|').toString();
    }
}
