import java.math.*;
import java.util.*;

final class MeasureMaker{
    static interface GetWhatSuffix{
	String getWhat(boolean is_lhs,boolean is_rhs);
	default String getSuffix(boolean is_lhs,boolean is_rhs){
	    return "";
	}
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
    void make(Rational t,GetWhatSuffix gws){
	dm.addDuration(t.subtract(time));
	time = t;
	boolean is_lhs=true;
	while (dm.duration.signum()!=0){
	    boolean is_rhs=dm.subtractDuration();
	    sb.append(dm.cat(gws.getWhat(is_lhs,is_rhs),gws.getSuffix(is_lhs,is_rhs)));
	    is_lhs = false;
	}
    }
    static final GetWhatSuffix REST=new GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return "r";
	}
    };
    static final GetWhatSuffix SKIP=new GetWhatSuffix(){
	@Override public String getWhat(boolean is_lhs,boolean is_rhs){
	    return "\\skip";
	}
    };
    String tail(){
	return sb.append(dm.tail()).append('|').toString();
    }
}
