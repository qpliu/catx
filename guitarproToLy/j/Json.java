import java.io.*;
import java.util.*;

final class Json implements Comparable<Json>{
    TreeMap<Json,Json>map;
    List<Json>list;
    String string;
    Double doubl;
    Boolean boolea;
    Character character;
    boolean isNull;
    String unparse(){
	StringBuilder sb=new StringBuilder();
	unparse(sb,"");
	return sb.toString();
    }
    void unparse(StringBuilder sb,String indent){
	if (map!=null){
	    sb.append('{');
	    if (map.size()!=0){
		sb.append("\n    ").append(indent);
		for (Iterator<Map.Entry<Json,Json>>i=map.entrySet().iterator(); i.hasNext();){
		    Map.Entry<Json,Json>me=i.next();
		    me.getKey().unparse(sb,indent+"    ");
		    sb.append(':');
		    me.getValue().unparse(sb,indent+"    ");
		    if (i.hasNext())
			sb.append(",\n    ").append(indent);
		}
		sb.append('\n').append(indent);
	    }
	    sb.append('}');
	}else if (list!=null){
	    sb.append('[');
	    if (list.size()!=0){
		sb.append("\n    ").append(indent);
		for (Iterator<Json>i=list.iterator(); i.hasNext();){
		    Json a=i.next();
		    a.unparse(sb,indent+"    ");
		    if (i.hasNext())
			sb.append(",\n    ").append(indent);
		}
		sb.append('\n').append(indent);
	    }
	    sb.append(']');
	}else if (string!=null)
	    sb.append('"').append(string).append('"');
	else if (doubl!=null){
	    if ((double)doubl==(int)(double)doubl)
		sb.append((int)(double)doubl);
	    else
		sb.append(doubl);
	}else if (boolea!=null)
	    sb.append(boolea);
	else if (character!=null)
	    throw new RuntimeException();
	else
	    sb.append("null");
    }
    static Json parse(String inputString){
	Parser p=new Parser(inputString);
	try{
	    return p.parse();
	}catch (RuntimeException e){
	    System.err.println("input="+p.inputString.substring(0,p.inputIndex)+" XXX "+p.inputString.substring(p.inputIndex));
	    throw e;
	}
    }
    private static class Parser{
	private String inputString;
	private int inputIndex;
	private Json ungetToken;
	private Parser(String inputString){
	    this.inputString = inputString;
	}
	private int getChar(){
	    if (inputIndex<inputString.length())
		return inputString.charAt(inputIndex++);
	    return -1;
	}
	private void ungetChar(int c){
	    --inputIndex;
	}
	private Json getToken(){
	    if (ungetToken!=null)	
		try{
		    return ungetToken;
		}finally{
		    ungetToken = null;
		}
	    for (;;){
		int i=getChar();
		if (i==-1)
		    return null;
		if (i=='\r' || i=='\n' || i==' ' || i=='\t')
		    continue;
		if (i=='"'){
		    StringBuilder sb=new StringBuilder();
		    boolean escape=false;
		    for (;;){
			i = getChar();
			if (escape)
			    escape = false;
			else if (i=='\\')
			    escape = true;
			else if (i=='"')
			    break;
			sb.append((char)i);
		    }
		    return new Json(sb.toString());
		}
		if (i=='[')/*]*/
		    return new Json(new ArrayList<Json>());
		if (i=='{')/*}*/
		    return new Json(new TreeMap<Json,Json>());
		if (i>='0' && i<='9' || i=='-' || i=='+' || i=='.'){
		    StringBuilder sb=new StringBuilder();
		    do
			sb.append((char)i);
		    while ((i=getChar())>='0' && i<='9' || i=='.' || i=='e' || i=='E' || i=='+' || i=='-');
		    ungetChar(i);
		    return new Json(Double.parseDouble(sb.toString()));
		}
		if (Character.isJavaIdentifierStart(i)){
		    StringBuilder sb=new StringBuilder();
		    do
			sb.append((char)i);
		    while (Character.isJavaIdentifierPart(i=getChar()));
		    ungetChar(i);
		    String identifier=sb.toString();
		    if (identifier.equals("true"))
			return new Json(true);
		    if (identifier.equals("false"))
			return new Json(false);
		    if (identifier.equals("null"))
			return new Json();
		    throw new RuntimeException("identifier="+identifier);
		}
		return new Json((char)i);
	    }
	}
	private void ungetToken(Json token){
	    ungetToken = token;
	}
	Json parse(){
	    Json o=getToken();
	    if (o.map!=null)
		for (;;){
		    Json oo=getToken();
		    if (oo.character!=null && oo.character==/*{*/'}')
			return o;
		    ungetToken(oo);
		    Json k=parse();
		    oo = getToken();
		    if (oo.character==null || oo.character!=':')
			throw new RuntimeException();
		    o.map.put(k,parse());
		    oo = getToken();
		    if (oo.character!=null && oo.character==/*{*/'}')
			return o;
		    if (oo.character==null || oo.character!=',')
			throw new RuntimeException();
		}
	    if (o.list!=null)
		for (;;){
		    Json oo=getToken();
		    if (oo.character!=null && oo.character==/*[*/']')
			return o;
		    ungetToken(oo);
		    o.list.add(parse());
		    oo = getToken();
		    if (oo.character!=null && oo.character==/*[*/']')
			return o;
		    if (oo.character==null || oo.character!=',')
			throw new RuntimeException();
		}
	    return o;
	}
    }
    private Json(){
	isNull = true;
    }
    private Json(TreeMap<Json,Json>map){
	this.map = map;
    }
    private Json(List<Json>list){
	this.list = list;
    }
    private Json(String string){
	this.string = string;
    }
    private Json(double doubl){
	this.doubl = doubl;
    }
    private Json(boolean boolea){
	this.boolea = boolea;
    }
    private Json(char character){
	this.character = character;
    }
    String stringValue(){
	StringBuilder sb=new StringBuilder();
	for (int i=0; i<string.length(); i++){
	    char c=string.charAt(i);
	    if (c=='\\'){
		c = string.charAt(++i);
		if (c=='b')
		    sb.append('\b');
		else if (c=='f')
		    sb.append('\f');
		else if (c=='n')
		    sb.append('\n');
		else if (c=='r')
		    sb.append('\r');
		else if (c=='t')
		    sb.append('\t');
		else if (c>='0'&&c<='7'){
		    int o=c-'0';
		    if (i+1<string.length() && string.charAt(i+1)>='0' && string.charAt(i+1)<='7')
			o = o*8+string.charAt(++i)-'0';
		    if (i+1<string.length() && string.charAt(i+1)>='0' && string.charAt(i+1)<='7')
			o = o*8+string.charAt(++i)-'0';
		    sb.append((char)o);
		}else if (c=='x'){
		    sb.append((char)Integer.parseInt(string.substring(i+1,i+3),16));
		    i += 2;
		}else if (c=='u'){
		    sb.append((char)Integer.parseInt(string.substring(i+1,i+5),16));
		    i += 4;
		}else
		    sb.append(c);
	    }else
		sb.append(c);
	}
	return sb.toString();
    }
    boolean booleanValue(){
	return boolea.booleanValue();
    }
    int intValue(){
	return (int)doubl.doubleValue();
    }
    Json get(String key){
	return get(new Json(key));
    }
    Json get(Json key){
	return map.get(key);
    }
    int size(){
	return list.size();
    }
    Json get(int index){
	return list.get(index);
    }
    public static void main(String[]argv)throws IOException{
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	for (int i; (i=System.in.read())!=-1; baos.write(i));
	Json json=parse(baos.toString());
	System.out.println(json.unparse());
    }
    String getType(){
	if (isNull)
	    return "null";
	if (map!=null)
	    return "map";
	if (list!=null)
	    return "list";
	if (string!=null)
	    return "string";
	if (doubl!=null)
	    return "double";
	if (boolea!=null)
	    return "boolean";
	throw new RuntimeException();
    }
    @Override public int hashCode(){
	return unparse().hashCode();
    }
    @Override public boolean equals(Object o){
	return o instanceof Json && compareTo((Json)o)==0;
    }
    @Override public int compareTo(Json a){
	return unparse().compareTo(a.unparse());
    }
}
