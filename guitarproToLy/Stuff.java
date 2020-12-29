import java.util.*;

final class Stuff{
    private static String[]scale={"c","cis","d","dis","e","f","fis","g","gis","a","ais","b"};
    static void setScale(String s){
	for (StringTokenizer st=new StringTokenizer(s); st.hasMoreTokens();){
	    String ly=st.nextToken();
	    scale[ly2midi(ly)%12] = ly;
	}
    }
    static String midi2ly(int key){
	return midi2ly(key,null);
    }
    static String midi2ly(int key,State state){
	int last=0;
	if (state!=null && state.isRelative)
	    last = state.lastRelative;
	String ly=scale[key%12];
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
	for (; last<next-3; last+=7)
	    ly += '\'';
	for (; last>next+3; last-=7)
	    ly += ',';
	if (state!=null && state.isRelative)
	    state.lastRelative = next;
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
}
