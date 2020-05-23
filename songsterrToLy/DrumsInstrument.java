import java.util.*;

final class DrumsInstrument extends Instrument{
    private static Map<Integer,Drum>drumMap=new HashMap<Integer,Drum>();
    private static class Drum implements Note{
	final int note;
	final String name;
	final int voice;
	final int sort;
	Drum(int note,String name,int voice){
	    this.note = note;
	    this.name = name;
	    this.voice = voice;
	    this.sort = drumMap.size();
	    drumMap.put(note,this);
	}
	@Override public int compareTo(Note n){
	    Drum d=(Drum)n;
	    if (sort!=d.sort)
		return Integer.compare(sort,d.sort);
	    return toString().compareTo(d.toString());
	}
	@Override public String toString(){
	    return note+','+name+','+voice;
	}
	@Override public String getLyNote(){
	    return name;
	}
    }
    static{
	new Drum(49,"cymc",0);
	new Drum(51,"cymr",0);
	new Drum(52,"cymch",0);
	new Drum(55,"cyms",0);
	new Drum(57,"cymcb",0);
	new Drum(59,"cymrb",0);
	new Drum(38,"sn",0);
	new Drum(40,"sne",0);
	new Drum(50,"tomh",0);
	new Drum(48,"tommh",0);
	new Drum(47,"tomml",0);
	new Drum(45,"toml",0);
	new Drum(43,"tomfh",0);
	new Drum(41,"tomfl",0);
	new Drum(42,"hh",0);
	new Drum(46,"hho",0);
	new Drum(35,"bda",1);
	new Drum(36,"bd",1);
	new Drum(37,"ss",0);
	new Drum(39,"hc",0);
	new Drum(44,"hhp",1);
	new Drum(53,"rb",0);
	new Drum(54,"tamb",0);
	new Drum(56,"cb",0);
	new Drum(58,"vibs",0);
	new Drum(60,"boh",0);
	new Drum(61,"bol",0);
	new Drum(62,"cghm",0);
	new Drum(63,"cgh",0);
	new Drum(64,"cgl",0);
	new Drum(65,"timh",0);
	new Drum(66,"timl",0);
	new Drum(67,"agh",0);
	new Drum(68,"agl",0);
	new Drum(69,"cab",0);
	new Drum(70,"mar",0);
	new Drum(71,"whs",0);
	new Drum(72,"whl",0);
	new Drum(73,"guis",0);
	new Drum(74,"guil",0);
	new Drum(75,"cl",0);
	new Drum(76,"wbh",0);
	new Drum(77,"wbl",0);
	new Drum(78,"cuim",0);
	new Drum(79,"cuio",0);
	new Drum(80,"trim",0);
	new Drum(81,"tri",0);
    }
    @Override Note getNote(Json note){
	Json fret=note.get("fret");
	if (fret==null)
	    return null;
	return drumMap.get(fret.intValue());
    }
    @Override boolean matches(State state){
	Json j;
	return (j=state.track.get("isDrums"))!=null && j.booleanValue();
    }
    @Override void printHead(){
	indent("drsPart = \\new DrumVoice = \"drsb\" \\new DrumVoice = \"drsa\" \\drummode {"); //}
	print("\\context DrumVoice = \"drsa\" \\voiceOne");
	print("\\context DrumVoice = \"drsb\" \\voiceTwo");
    }
    @Override String notesToString(List<Event>list){
	List<Event>list0=new ArrayList<Event>();
	List<Event>list1=new ArrayList<Event>();
	for (Event e:list){
	    if (e.note!=null && ((Drum)e.note).voice==0)
		list0.add(e);
	    if (e.note!=null && ((Drum)e.note).voice==1)
		list1.add(e);
	}
	StringBuilder sb=new StringBuilder();
	if (list0.size()==0)
	    sb.append(super.notesToString(list1));
	else if (list1.size()==0)
	    sb.append(super.notesToString(list0));
	else{
	    sb.append("<< { ");
	    sb.append(super.notesToString(list0));
	    sb.append(" } \\context DrumVoice = \"drsb\" { ");
	    sb.append(super.notesToString(list1));
	    sb.append(" } >>");
	}
	return sb.toString();
    }
}
