import java.util.*;

final class Stuff{
    static String midi2ly(int key){
	return midi2ly(key,null);
    }
    static String midi2ly(int key,Arg arg){
	String ly=arg.scale[key%12];
	int next=(ly.charAt(0)-'a'+5)%7-3;
	if (ly.endsWith("'")){
	    ly = ly.substring(0,ly.length()-1);
	    next += 7;
	}
	if (ly.endsWith(",")){
	    ly = ly.substring(0,ly.length()-1);
	    next -= 7;
	}
	while (key<48){
	    key += 12;
	    next -= 7;
	}
	while (key>=60){
	    key -= 12;
	    next += 7;
	}
	int last=0;
	for (; last<next-3; last+=7)
	    ly += '\'';
	for (; last>next+3; last-=7)
	    ly += ',';
	return ly;
    }
    static int ly2midi(String ly){
	int key=48;
	for (; ly.endsWith(","); ly=ly.substring(0,ly.length()-1))
	    key -= 12;
	for (; ly.endsWith("'"); ly=ly.substring(0,ly.length()-1))
	    key += 12;
	for (; ly.endsWith("es"); ly=ly.substring(0,ly.length()-2))
	    key -= 1;
	for (; ly.endsWith("is"); ly=ly.substring(0,ly.length()-2))
	    key += 1;
	if (ly.equals("d"))
	    key += 2;
	else if (ly.equals("e"))
	    key += 4;
	else if (ly.equals("f"))
	    key += 5;
	else if (ly.equals("g"))
	    key += 7;
	else if (ly.equals("a"))
	    key += 9;
	else if (ly.equals("b"))
	    key += 11;
	else if (!ly.equals("c"))
	    throw new RuntimeException("Bad note "+ly);
	return key;
    }
    static String escape(String s){
	StringBuilder sb=new StringBuilder("\"");
	for (int i=0; i<s.length(); i++){
	    if (s.charAt(i)=='\\' || s.charAt(i)=='"')
		sb.append('\\');
	    sb.append(s.charAt(i));
	}
	return sb.append('"').toString();
    }
    private static final String[]sharpsToLy={"fes","ces","ges","des","aes","ees","bes","f","c","g","d","a","e","b","fis","cis","gis","dis","ais","eis"};
    static String keyToLy(int key0,int key1){
	return "\\key "+sharpsToLy[key0+(key1==0?8:11)]+(key1==0?" \\major":" \\minor");
    }
    static Gpfile.Event[]cutEvent(Gpfile.Event e,Rational cut){
	if (e.time.compareTo(cut)>=0)
	    return new Gpfile.Event[]{null,e};
	if (e.time.add(e.duration).compareTo(cut)<=0)
	    return new Gpfile.Event[]{e,null};
	Gpfile.Event lhs=e.clone();
	lhs.tie_lhs = true;
	lhs.duration = cut.subtract(lhs.time);
	Gpfile.Event rhs=e.clone();
	rhs.tie_rhs = true;
	rhs.time = cut;
	rhs.duration = e.duration.subtract(lhs.duration);
	return new Gpfile.Event[]{lhs,rhs};
    }
}
