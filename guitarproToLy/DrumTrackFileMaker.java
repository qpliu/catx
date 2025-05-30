import java.io.*;
import java.util.*;

final class DrumTrackFileMaker extends SuperTrackFileMaker{
    private static final int VOICES=2;
    private static final String DEFAULT_DRUM_MAP=
	"42 hh 0,"+
	"46 hho 0,"+
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
    private static class Drum{
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
    }
    @Override boolean filterEvents(Gpfile.Event event){
	return !event.tie_rhs && super.filterEvents(event);
    }
    private static class EventAndDrum implements Comparable<EventAndDrum>{
	final Gpfile.NoteEvent event;
	final Drum drum;
	EventAndDrum(Gpfile.NoteEvent event,Drum drum){
	    this.event = event;
	    this.drum = drum;
	}
	@Override public int compareTo(EventAndDrum d){
	    int i;
	    if ((i=event.time.compareTo(d.event.time))!=0)
		return i;
	    if ((i=Integer.compare(drum.sort,d.drum.sort))!=0)
		return i;
	    return drum.name.compareTo(d.drum.name);
	}
    }
    private final Map<Integer,Drum>drumMap=new HashMap<Integer,Drum>();
    DrumTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,"colors",arg);
	putDrumMap(DEFAULT_DRUM_MAP);
	if (arg.drumMap!=null)
	    putDrumMap(arg.drumMap);
	Stuff.add(arg.music_extra,0,"\\context DrumVoice = \"drsa\" \\voiceOne");
	Stuff.add(arg.music_extra,0,"\\context DrumVoice = \"drsb\" \\voiceTwo");
    }
    private void putDrumMap(String map){
	for (StringTokenizer st=new StringTokenizer(map,","); st.hasMoreTokens();){
	    StringTokenizer st2=new StringTokenizer(st.nextToken());
	    int key=Integer.parseInt(st2.nextToken());
	    if (st2.hasMoreTokens()){
		String name=st2.nextToken();
		int voice=Integer.parseInt(st2.nextToken());
		Drum old=drumMap.get(key);
		drumMap.put(key,new Drum(key,name,voice,old==null?drumMap.size():old.sort));
	    }else
		drumMap.remove(key);
	}
    }
    @Override String getStuff(){
	return super.getStuff()+"\\new DrumVoice = \"drsb\" \\new DrumVoice = \"drsa\" \\drummode ";
    }
    private String makeVoice(Gpfile.Measure measure,Queue<EventAndDrum>voiceEvents)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	while (voiceEvents.size()!=0){
	    EventAndDrum ead=voiceEvents.poll();
	    mm.make(ead.event.time,MeasureMaker.REST);
	    List<EventAndDrum>l=new ArrayList<EventAndDrum>();
	    l.add(ead);
	    while (voiceEvents.size()!=0 && voiceEvents.peek().event.time.equals(ead.event.time))
		l.add(voiceEvents.poll());
	    StringBuilder sb=new StringBuilder();
	    if (l.size()>1)
		sb.append('<');
	    for (int i=0; i<l.size(); i++){
		EventAndDrum a=l.get(i);
		sb.append(i==0?"":" ");
		if (a.event.is_ghost)
		    sb.append("\\parenthesize ");
		sb.append(a.drum.name);
	    }
	    if (l.size()>1)
		sb.append('>');
	    Rational next;
	    if (voiceEvents.size()==0)
		next = measure.time.add(measure.time_n);
	    else
		next = voiceEvents.peek().event.time;
	    mm.make(next,new MeasureMaker.GetWhatSuffix(){
		@Override public String getWhat(boolean is_lhs,boolean is_rhs){
		    return is_lhs?sb.toString():"r";
		}
	    });
	}
	mm.make(measure.time.add(measure.time_n),MeasureMaker.REST);
	return mm.tail();
    }
    @Override String makeMeasure(Gpfile.Measure measure,List<Gpfile.Event>measureEvents)throws IOException{
	Queue<Gpfile.Event>filtered=getFilteredEvents(measureEvents);
	List<Queue<EventAndDrum>>list=new ArrayList<Queue<EventAndDrum>>();
	int nonEmptyCount=0;
	for (int voice=0; voice<VOICES; voice++){
	    Queue<EventAndDrum>q=new PriorityQueue<EventAndDrum>();
	    list.add(q);
	    for (Gpfile.Event e:filtered){
		int k=((Gpfile.NoteEvent)e).getNote();
		Drum drum=drumMap.get(k);
		if (drum==null)
		    Log.info("Drum %d not in drumMap",k);
		if (drum!=null && drum.voice==voice)
		    q.add(new EventAndDrum((Gpfile.NoteEvent)e,drum));
	    }
	    if (q.size()!=0)
		nonEmptyCount++;
	}
	if (nonEmptyCount<2)
	    return makeVoice(measure,list.get(list.get(0).size()==0?1:0));
	StringBuilder sb=new StringBuilder("<< { ");
	sb.append(makeVoice(measure,list.get(0)));
	sb.append(" } \\context DrumVoice = \"drsb\" { ");
	sb.append(makeVoice(measure,list.get(1)));
	return sb.append(" } >>").toString();
    }
    @Override MeasureMaker.GetWhatSuffix getGetWhatSuffix(List<Gpfile.Event>list){
	throw new RuntimeException();
    }
}
