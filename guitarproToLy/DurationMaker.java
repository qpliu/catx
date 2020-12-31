import java.math.*;

final class DurationMaker{
    private final int time_d;
    private Rational tuplet=Rational.ONE;
    Rational duration=Rational.ZERO;
    private String lastAfter;
    private String before;
    private String after;
    DurationMaker(int time_d){
	this.time_d = time_d;
    }
    DurationMaker addDuration(Rational d){
	duration = duration.add(d);
	return this;
    }
    boolean subtractDuration(){
	Rational t=new Rational(duration.d,BigInteger.ONE);
	while (!t.n.testBit(0))
	    t = new Rational(t.n.shiftRight(1),BigInteger.ONE);
	while (t.compareTo(Rational.TWO)>0)
	    t = t.divide(2);
	StringBuilder before_sb=new StringBuilder();
	if (!t.equals(tuplet) && !tuplet.equals(Rational.ONE)){
	    before_sb.append(/*{*/"} ");
	    lastAfter = null;
	}
	if (!t.equals(tuplet) && !t.equals(Rational.ONE)){
	    before_sb.append("\\tuplet "+t+" { "); //}
	    lastAfter = null;
	}
	tuplet = t;
	duration = duration.multiply(tuplet);
	Rational d=new Rational(time_d);
	BigInteger n=BigInteger.ONE;
	while (d.compareTo(duration)>0){
	    n = n.shiftLeft(1);
	    d = d.divide(2);
	}
	StringBuilder after_sb=new StringBuilder(n.toString());
	duration = duration.subtract(d);
	if (duration.compareTo(d.divide(2))>=0){
	    after_sb.append('.');
	    duration = duration.subtract(d.divide(2));
	}
	duration = duration.divide(tuplet);
	before = before_sb.toString();
	after = after_sb.toString();
	return duration.signum()==0;
    }
    String cat(String what,String suffix){
	if (what.equals("\\skip"))
	    lastAfter = null;
	else if (after.equals(lastAfter))
	    after = "";
	else
	    lastAfter = after;
	return before+what+after+suffix+' ';
    }
    String tail(){
	return tuplet.equals(Rational.ONE)?"":/*{*/"} ";
    }
}
