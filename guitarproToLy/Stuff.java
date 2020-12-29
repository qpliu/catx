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
}
