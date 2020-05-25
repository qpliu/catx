import java.math.*;

final class Rational implements Comparable<Rational>{
    static final Rational ZERO=new Rational(0);
    static final Rational TWO=new Rational(2);
    final BigInteger n,d;
    Rational(long n){
	this(n,1);
    }
    Rational(long n,long d){
	this(BigInteger.valueOf(n),BigInteger.valueOf(d));
    }
    Rational(BigInteger n,BigInteger d){
	if (d.signum()==0)
	    throw new RuntimeException();
	if (d.signum()<0){
	    d = d.negate();
	    n = n.negate();
	}
	BigInteger gcd=n.gcd(d);
	this.n = n.divide(gcd);
	this.d = d.divide(gcd);
    }
    Rational add(Rational r){
	return new Rational(n.multiply(r.d).add(r.n.multiply(d)),d.multiply(r.d));
    }
    Rational subtract(Rational r){
	return add(r.multiply(-1));
    }
    Rational multiply(Rational r){
	return new Rational(n.multiply(r.n),d.multiply(r.d));
    }
    Rational divide(Rational r){
	return new Rational(n.multiply(r.d),d.multiply(r.n));
    }
    Rational add(long l){
	return add(new Rational(l));
    }
    Rational multiply(long l){
	return multiply(new Rational(l));
    }
    Rational divide(long l){
	return divide(new Rational(l));
    }
    int signum(){
	return n.signum();
    }
    @Override public int compareTo(Rational r){
	return subtract(r).signum();
    }
    @Override public String toString(){
	if (d.equals(BigInteger.ONE))
	    return n.toString();
	return n+"/"+d;
    }
    @Override public int hashCode(){
	return toString().hashCode();
    }
    @Override public boolean equals(Object o){
	return o!=null&&toString().equals(o.toString());
    }
}
