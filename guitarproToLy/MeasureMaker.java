import java.math.*;
import java.util.*;

final class MeasureMaker{
    static interface GetWhatSuffix{
	String getWhat(boolean is_lhs,boolean is_rhs); 
	String getSuffix(boolean is_lhs,boolean is_rhs); 
    }
    final Gpfile.Measure measure;
    Rational time;
    private final StringBuilder sb=new StringBuilder();
    private final DurationMaker dm;
    MeasureMaker(Gpfile.Measure measure){
	this.measure = measure;
	time = measure.time;
	dm = new DurationMaker(measure.time_d);
    }
    void make(Rational t,GetWhatSuffix gws,boolean skip){
	dm.addDuration(t.subtract(time));
	time = t;
	boolean is_lhs=true;
	while (dm.duration.signum()!=0){
	    String[]ba=dm.subtractDuration(skip);
	    boolean is_rhs=dm.duration.signum()==0;
	    sb.append(ba[0]).append(gws.getWhat(is_lhs,is_rhs)).append(ba[1]).append(gws.getSuffix(is_lhs,is_rhs)).append(' ');
	    is_lhs = false;
	}
    }
    void make(Rational t,String what,boolean skip){
	make(t,new GetWhatSuffix(){
	    @Override public String getWhat(boolean is_lhs,boolean is_rhs){
		return what;
	    }
	    @Override public String getSuffix(boolean is_lhs,boolean is_rhs){
		return "";
	    }
	},skip);
    }
    void skip(Rational t){
	make(t,"\\skip",true);
    }
    void rest(Rational t){
	make(t,"r",false);
    }
    String tail(){
	return sb.append(dm.tail()).append('|').toString();
    }
}
