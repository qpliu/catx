import java.math.*;
import java.util.*;

final class NotesToStringText extends NotesToString{
    NotesToStringText(State state){
	super(state,"s");
    }
    @Override Rational notesToString(List<Event>l,Rational duration){
	appendRest(duration);
	return Rational.ZERO;
    }
}
