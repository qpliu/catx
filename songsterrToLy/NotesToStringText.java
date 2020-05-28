import java.math.*;
import java.util.*;

final class NotesToStringText extends NotesToString{
    NotesToStringText(State state){
	super(state,"s");
    }
    @Override void notesToString(List<Event>l,String duration){
	sb.append(rest).append(duration);
    }
}
