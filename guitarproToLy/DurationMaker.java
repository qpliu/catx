import java.math.*;

final class DurationMaker{
    final int time_d;
    Rational tuplet=Rational.ONE;
    Rational duration=Rational.ZERO;
    String lastDuration;
    DurationMaker(int time_d){
	this.time_d = time_d;
    }
    DurationMaker addDuration(Rational d){
	duration = duration.add(d);
	return this;
    }
    String[]subtractDuration(boolean skip){
	Rational t=new Rational(duration.d,BigInteger.ONE);
	while (!t.n.testBit(0))
	    t = new Rational(t.n.shiftRight(1),BigInteger.ONE);
	while (t.compareTo(Rational.TWO)>0)
	    t = t.divide(2);
	StringBuilder before=new StringBuilder();
	if (!t.equals(tuplet) && !tuplet.equals(Rational.ONE)){
	    before.append(/*{*/"} ");
	    lastDuration = null;
	}
	if (!t.equals(tuplet) && !t.equals(Rational.ONE)){
	    before.append("\\tuplet "+t+" { "); //}
	    lastDuration = null;
	}
	tuplet = t;
	duration = duration.multiply(tuplet);
	Rational d=new Rational(time_d);
	BigInteger n=BigInteger.ONE;
	while (d.compareTo(duration)>0){
	    n = n.shiftLeft(1);
	    d = d.divide(2);
	}
	StringBuilder after=new StringBuilder(n.toString());
	duration = duration.subtract(d);
	if (duration.compareTo(d.divide(2))>=0){
	    after.append('.');
	    duration = duration.subtract(d.divide(2));
	}
	duration = duration.divide(tuplet);
	String a=after.toString();
	if (skip)
	    lastDuration = null;
	else if (a.equals(lastDuration))
	    a = "";
	else
	    lastDuration = a;
	return new String[]{before.toString(),a};
    }
    String tail(){
	return tuplet.equals(Rational.ONE)?"":/*{*/"} ";
    }
}
