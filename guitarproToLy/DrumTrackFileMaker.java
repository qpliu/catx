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
    private static class TimeAndDrum implements Comparable<TimeAndDrum>{
	final Rational time;
	final Drum drum;
	TimeAndDrum(Rational time,Drum drum){
	    this.time = time;
	    this.drum = drum;
	}
	@Override public int compareTo(TimeAndDrum d){
	    int i;
	    if ((i=time.compareTo(d.time))!=0)
		return i;
	    if ((i=Integer.compare(drum.sort,d.drum.sort))!=0)
		return i;
	    return drum.name.compareTo(d.drum.name);
	}
    }
    private final Map<Integer,Drum>drumMap=new HashMap<Integer,Drum>();
    DrumTrackFileMaker(Main main,Arg arg)throws IOException{
	super(main,"colors",arg,Gpfile.NoteEvent.class,MeasureMaker.REST);
	for (StringTokenizer st=new StringTokenizer(arg.drumMap==null?DEFAULT_DRUM_MAP:arg.drumMap,","); st.hasMoreTokens();){
	    StringTokenizer st2=new StringTokenizer(st.nextToken());
	    int key=Integer.parseInt(st2.nextToken());
	    String name=st2.nextToken();
	    int voice=Integer.parseInt(st2.nextToken());
	    drumMap.put(key,new Drum(key,name,voice,drumMap.size()));
	}
    }
    @Override void make()throws IOException{
	indent(lyname+" = \\new DrumVoice = \"drsb\" \\new DrumVoice = \"drsa\" \\drummode {");
	print("\\context DrumVoice = \"drsa\" \\voiceOne");
	print("\\context DrumVoice = \"drsb\" \\voiceTwo");
	makeMeasures();
	unindent("}");
    }
    private String makeVoice(Gpfile.Measure measure,Queue<TimeAndDrum>events)throws IOException{
	MeasureMaker mm=new MeasureMaker(measure);
	while (events.size()!=0){
	    TimeAndDrum tad=events.poll();
	    mm.make(tad.time,MeasureMaker.REST);
	    List<TimeAndDrum>l=new ArrayList<TimeAndDrum>();
	    l.add(tad);
	    while (events.size()!=0 && events.peek().time.equals(tad.time))
		l.add(events.poll());
	    StringBuilder sb=new StringBuilder();
	    if (l.size()>1)
		sb.append('<');
	    for (int i=0; i<l.size(); i++)
		sb.append(i==0?"":" ").append(l.get(i).drum.name);
	    if (l.size()>1)
		sb.append('>');
	    Rational next;
	    if (events.size()==0)
		next = measure.time.add(measure.time_n);
	    else
		next = events.peek().time;
	    mm.make(next,new MeasureMaker.GetWhatSuffix(){
		@Override public String getWhat(boolean is_lhs,boolean is_rhs){
		    return is_lhs?sb.toString():"r";
		}
	    });
	}
	mm.make(measure.time.add(measure.time_n),MeasureMaker.REST);
	return mm.tail();
    }
    @Override String makeMeasure(Gpfile.Measure measure,List<Gpfile.Event>events)throws IOException{
	List<Queue<TimeAndDrum>>list=new ArrayList<Queue<TimeAndDrum>>();
	int nonEmptyCount=0;
	for (int voice=0; voice<VOICES; voice++){
	    Queue<TimeAndDrum>q=new PriorityQueue<TimeAndDrum>();
	    list.add(q);
	    for (Gpfile.Event e:events)
		if (!e.tie_rhs && e instanceof Gpfile.NoteEvent){
		    Drum drum=drumMap.get(((Gpfile.NoteEvent)e).getNote());
		    if (drum!=null && drum.voice==voice)
			q.add(new TimeAndDrum(e.time,drum));
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
}
