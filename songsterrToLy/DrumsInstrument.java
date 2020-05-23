import java.util.*;

final class DrumsInstrument extends Instrument{
    private final Map<Integer,Drum>drumMap=new HashMap<Integer,Drum>();
    private static class Drum implements Note{
	final int note;
	final String name;
	final int voice;
	final int sort;
	Drum(int note,String name,int voice,int sort){
	    this.note = note;
	    this.name = name;
	    this.voice = voice;
	    this.sort = sort;
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
    private static final String DEFAULT_DRUM_MAP=
	"49 cymc 0,"+
	"51 cymr 0,"+
	"52 cymch 0,"+
	"55 cyms 0,"+
	"57 cymcb 0,"+
	"59 cymrb 0,"+
	"38 sn 0,"+
	"40 sne 0,"+
	"50 tomh 0,"+
	"48 tommh 0,"+
	"47 tomml 0,"+
	"45 toml 0,"+
	"43 tomfh 0,"+
	"41 tomfl 0,"+
	"42 hh 0,"+
	"46 hho 0,"+
	"35 bda 1,"+
	"36 bd 1,"+
	"37 ss 0,"+
	"39 hc 0,"+
	"44 hhp 1,"+
	"53 rb 0,"+
	"54 tamb 0,"+
	"56 cb 0,"+
	"58 vibs 0,"+
	"60 boh 0,"+
	"61 bol 0,"+
	"62 cghm 0,"+
	"63 cgh 0,"+
	"64 cgl 0,"+
	"65 timh 0,"+
	"66 timl 0,"+
	"67 agh 0,"+
	"68 agl 0,"+
	"69 cab 0,"+
	"70 mar 0,"+
	"71 whs 0,"+
	"72 whl 0,"+
	"73 guis 0,"+
	"74 guil 0,"+
	"75 cl 0,"+
	"76 wbh 0,"+
	"77 wbl 0,"+
	"78 cuim 0,"+
	"79 cuio 0,"+
	"80 trim 0,"+
	"81 tri 0";
    @Override void setState(State state){
	super.setState(state);
	for (StringTokenizer st=new StringTokenizer(state.drumMap==null?DEFAULT_DRUM_MAP:state.drumMap,","); st.hasMoreTokens();){
	    StringTokenizer st2=new StringTokenizer(st.nextToken());
	    int key=Integer.parseInt(st2.nextToken());
	    String name=st2.nextToken();
	    int voice=Integer.parseInt(st2.nextToken());
	    drumMap.put(key,new Drum(key,name,voice,drumMap.size()));
	}
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
	indent(state.partName+" = \\new DrumVoice = \"drsb\" \\new DrumVoice = \"drsa\" \\drummode {"); //}
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
