import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

final class MergeMidi{
    private static final int DIVISION=384;
    private static final int DRUMS_CHANNEL=10-1;
    private static final double BEND_RANGE=24;
// WTF?  Lilypond adds an extra 48 divisions after each Lyric meta event?  But sometimes it is 144?
// Sometimes it is 192.  Sometimes 24.
    private static int DEFAULT_FUDGE_LYRICS=48;
    private static StringTokenizer fudgeLyricsStringTokenizer=new StringTokenizer("");
    private final Map<String,List<TextEvent>>textEvents=new HashMap<String,List<TextEvent>>();
    private final List<MetaEvent>metaEvents=new ArrayList<MetaEvent>();
    private final List<Event>events=new ArrayList<Event>();
    private final List<OutputEvent>outputEvents=new ArrayList<OutputEvent>();
    private final Set<TextEvent>unused=new HashSet<TextEvent>();
    private static final Pattern textPattern=Pattern.compile("([>!.-]*)(\\([^ ()]*\\))?(?:([a-zA-Z]+)=)?([a-zA-Z]+.*)");
    private static final Pattern whatParamPattern=Pattern.compile("([a-zA-Z]+)(.*)");
    private static final Pattern lyNotePattern=Pattern.compile("([a-g])((?:es|is)*)([',]*)");
    private class OutputEvent implements Comparable<OutputEvent>{
	final long time;
	final int sort;
	final int outTrackIndex;
	final int one;
	final byte[]bytes;
	OutputEvent(long time,int sort,int outTrackIndex,int one,byte...bytes){
	    this.time = time;
	    this.sort = sort;
	    this.outTrackIndex = outTrackIndex;
	    this.one = one;
	    this.bytes = bytes.clone();
	}
	@Override public int compareTo(OutputEvent e){
	    int i;
	    if ((i=Long.compare(time,e.time))!=0)
		return i;
	    return Integer.compare(sort,e.sort);
	}
    }
    private class Event implements Comparable<Event>{
	final long time;
	final int sort;
	final String id;
	final int outTrackIndex;
	Event(long time,int sort,String id,int outTrackIndex){
	    this.time = time;
	    this.sort = sort;
	    this.id = id;
	    this.outTrackIndex = outTrackIndex;
	}
	@Override public int compareTo(Event e){
	    return Long.compare(time,e.time)*2+Integer.compare(sort,e.sort);
	}
    }
    private String timeToStringAndPrintMap(PrintStream ps,long time){
	int numerator=1;
	int denominator=1;
	int microsecondsPerQuarterNote=500000;
	long seconds=0;
	long measure=0;
	Collections.sort(metaEvents);
	int i=0;
	int imeasure=0;
	for (long lastTime=0; lastTime<time;){
	    if (ps!=null && measure>=imeasure*4*DIVISION*numerator)
		ps.println(String.format("measure=%d seconds=%.3f divisions=%d",++imeasure,1e-6/DIVISION*seconds,lastTime));
	    long t=ps==null?time:lastTime+1;
	    if (i<metaEvents.size()){
		MetaEvent me=metaEvents.get(i);
		if (me.time<t)
		    t = me.time;
	    }
	    long delta=t-lastTime;
	    lastTime = t;
	    seconds += delta*microsecondsPerQuarterNote;
	    measure += delta*denominator;
	    if (i<metaEvents.size()){
		MetaEvent me=metaEvents.get(i);
		if (me.time==t){
		    i++;
		    if (me instanceof TempoEvent){
			microsecondsPerQuarterNote = ((TempoEvent)me).getMicrosecondsPerQuarterNote();
		    }else if (me instanceof TimeSignatureEvent){
			int n=((TimeSignatureEvent)me).getNumerator();
			int nn=numerator*n/BigInteger.valueOf(numerator).gcd(BigInteger.valueOf(n)).intValue();
			measure *= nn/numerator;
			denominator = nn/n<<((TimeSignatureEvent)me).getLogDenominator();
			numerator = nn;
		    }
		}
	    }
	}
	StringBuilder sb=new StringBuilder();
	sb.append("divisions=").append(time);
	sb.append(" seconds=").append(1e-6/DIVISION*seconds);
	sb.append(" measure=").append(1+(double)measure/(4*DIVISION*numerator));
	return sb.toString();
    }
    private void printMeasureToTime(PrintStream ps,long maxTime){
	timeToStringAndPrintMap(ps,maxTime);
    }
    private String timeToString(long time){
	if (time==Long.MAX_VALUE)
	    return "Long.MAX_VALUE";
	return timeToStringAndPrintMap(null,time);
    }
    private static double note2Midi(String s)throws IOException{
	try{
	    return Double.parseDouble(s);
	}catch (NumberFormatException e){}
	Matcher m=lyNotePattern.matcher(s);
	if (!m.matches())
	    throw new IOException("Bad note "+s);
	double k;
	String a=m.group(1);
	if (a.equals("c"))
	    k = 48;
	else if (a.equals("d"))
	    k = 50;
	else if (a.equals("e"))
	    k = 52;
	else if (a.equals("f"))
	    k = 53;
	else if (a.equals("g"))
	    k = 55;
	else if (a.equals("a"))
	    k = 57;
	else if (a.equals("b"))
	    k = 59;
	else
	    throw new RuntimeException();
	a = m.group(2);
	for (int i=0; i<a.length(); i+=2)
	    if (a.substring(i,i+2).equals("is"))
		k++;
	    else if (a.substring(i,i+2).equals("es"))
		--k;
	    else if (a.substring(i,i+2).equals("ih"))
		k += .5;
	    else if (a.substring(i,i+2).equals("eh"))
		k -= .5;
	    else
		throw new RuntimeException();
	a = m.group(3);
	for (int i=0; i<a.length(); i++)
	    if (a.charAt(i)=='\'')
		k += 12;
	    else if (a.charAt(i)==',')
		k -= 12;
	    else
		throw new RuntimeException();
	return k;
    }
    private static String[]makeParam(String s){
	List<String>l=new ArrayList<String>();
	for (StringTokenizer st=new StringTokenizer(s,":"); st.hasMoreTokens(); l.add(st.nextToken()));
	return l.toArray(new String[l.size()]);
    }
    private class TextEvent extends Event{
	final TextEvent parent;
	final long stop;
	final String what;
	final String[]param;
	final String trackName;
	final Set<Double>matchKey;
	final boolean matchStartTime;
	boolean done;
	final Map<String,Object>map=new HashMap<String,Object>();
	TextEvent(TextEvent parent,long start,String id,int outTrackIndex,long stop,String trackName,boolean matchStartTime,Set<Double>matchKey,String what,String[]param)throws IOException{
	    super(start,0,id,outTrackIndex);
	    this.parent = parent==null?this:parent;
	    this.stop = stop;
	    this.trackName = trackName;
	    this.matchStartTime = matchStartTime;
	    this.matchKey = matchKey;
	    this.what = what;
	    this.param = param;
	}
	boolean matches(NoteEvent note){
	    if (!trackName.equals(note.trackName))
		return false;
	    if (stop<=note.time)
		return false;
	    if (time>=note.stop)
		return false;
	    if (matchStartTime && time!=note.time)
		return false;
	    if (matchKey!=null && !matchKey.contains(note.key))
		return false;
	    return true;
	}
	boolean narrowerThan(TextEvent a){
	    if (matchStartTime!=a.matchStartTime)
		return matchStartTime;
	    if (time!=a.time)
		return time>a.time;
	    if (stop!=a.stop)
		return stop<a.stop;
	    if ((matchKey==null)!=(a.matchKey==null))
		return matchKey!=null;
	    return false;
	}
	@Override public String toString(){
	    StringBuilder sb=new StringBuilder("TextEvent");
	    sb.append(" start ").append(timeToString(time));
	    sb.append(" stop ").append(timeToString(stop));
	    sb.append(" whatparam=").append(what);
	    for (String p:param)
		sb.append(':').append(p);
	    sb.append(" trackName=").append(trackName);
	    sb.append(" matchKey=").append(matchKey);
	    sb.append(" matchStartTime=").append(matchStartTime);
	    return sb.toString();
	}
    }
    private class MetaEvent extends Event{
	final int what;
	final byte[]data;
	MetaEvent(long time,String id,int outTrackIndex,int what,byte[]data){
	    super(time,1,id,outTrackIndex);
	    this.what = what;
	    this.data = data.clone();
	}
    }
    private class LyricEvent extends MetaEvent{
	LyricEvent(long time,String id,int outTrackIndex,int what,byte[]data){
	    super(time,id,outTrackIndex,what,data);
//	    System.err.println(timeToStringAndPrintMap(null,time)+" lyric="+new String(data));
	}
    }
    private class TempoEvent extends MetaEvent{
	TempoEvent(long time,int outTrackIndex,String id,int what,byte[]data){
	    super(time,id,outTrackIndex,what,data);
	}
	int getMicrosecondsPerQuarterNote(){
	    return (data[0]&255)<<16|(data[1]&255)<<8|data[2]&255;
	}
    }
    private class TimeSignatureEvent extends MetaEvent{
	TimeSignatureEvent(long time,int outTrackIndex,String id,int what,byte[]data){
	    super(time,id,outTrackIndex,what,data);
	}
	int getNumerator(){
	    return data[0]&255;
	}
	int getLogDenominator(){
	    return data[1]&255;
	}
    }
    private class KeySignatureEvent extends MetaEvent{
	KeySignatureEvent(long time,int outTrackIndex,String id,int what,byte[]data){
	    super(time,id,outTrackIndex,what,data);
	}
    }
    private class ProgramChangeEvent extends Event{
	final int program;
	ProgramChangeEvent(long time,String id,int outTrackIndex,int program){
	    super(time,2,id,outTrackIndex);
	    this.program = program;
	}
    }
    private class BendEvent extends Event{
	final int amount;
	BendEvent(long time,String id,int outTrackIndex,int amount){
	    super(time,2,id,outTrackIndex);
	    this.amount = amount;
	}
    }
    private class NoteEvent extends Event{
	final long stop;
	final String trackName;
	double key;
	double velocity;
	boolean percussion;
	NoteEvent(long start,String id,int outTrackIndex,long stop,String trackName,double key,double velocity,boolean percussion){
	    super(start,2,id,outTrackIndex);
	    this.stop = stop;
	    this.trackName = trackName;
	    this.key = key;
	    this.velocity = velocity;
	    this.percussion = percussion;
	}
    }
    private void writeVlen(OutputStream os,int len)throws IOException{
	writeVlen(os,len,0);
    }
    private void writeVlen(OutputStream os,int len,int bit)throws IOException{
	if (len>127)
	    writeVlen(os,len>>7,128);
	os.write(len&127|bit);
    }
    private class TrackReader{
	private final DataInputStream dis;
	private final String id;
	private final int outTrackIndex;
	private boolean isTextTrack;
	private String trackName;
	private long time;
	private final long[]keyTime=new long[128];
	private final int[]keyVelocity=new int[128];
	private Map<Integer,String>textMap;
	private int fudgeLyrics;
	private int lyricEventTimeFudge=0;
	TrackReader(DataInputStream dis,String id,int outTrackIndex){
	    this.dis = dis;
	    this.id = id;
	    this.outTrackIndex = outTrackIndex;
	    Arrays.fill(keyTime,-1);
	}
	private int readVlen()throws IOException{
	    for (int v=0;;){
		int i=dis.readByte();
		v = v<<7|i&127;
		if (i>=0)
		    return v;
	    }
	}
	private boolean readMeta(int what,byte[]data)throws IOException{
	    if (what==0){
		System.err.println("Sequence Number="+((data[0]&255)<<8|data[1]&255));
	    }else if (what==1){
//		System.err.println("text="+new String(data));
	    }else if (what==2){
		System.err.println("copyright="+new String(data));
	    }else if (what==3){
		trackName = new String(data);
//		System.err.println("track name="+trackName);
		int i=trackName.indexOf(':');
		if (i!=-1)
		    trackName = trackName.substring(0,i);
		if (trackName.endsWith("_dotext")){
		    isTextTrack = true;
		    try (FileInputStream fis=new FileInputStream(trackName+"_notemap")){
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			for (int j; (j=fis.read())!=-1; baos.write(j));
			textMap = new HashMap<Integer,String>();
			for (StringTokenizer st=new StringTokenizer(baos.toString(),"\n"); st.hasMoreTokens();)
			    textMap.put(textMap.size(),st.nextToken());
		    }
		    trackName = trackName.substring(0,trackName.length()-7);
		}
	    }else if (what==4){
//		System.err.println("instrument name="+new String(data));
	    }else if (what==5){
		metaEvents.add(new LyricEvent(time+lyricEventTimeFudge,id,outTrackIndex,what,data));
		if (fudgeLyrics==0)
		    fudgeLyrics = fudgeLyricsStringTokenizer.hasMoreTokens()?Integer.parseInt(fudgeLyricsStringTokenizer.nextToken()):DEFAULT_FUDGE_LYRICS;
		lyricEventTimeFudge -= fudgeLyrics;
	    }else if (what==6){
		System.err.println("marker="+new String(data));
	    }else if (what==7){
		System.err.println("cue point="+new String(data));
	    }else if (what==0x20){
		System.err.println("Channel prefix "+(data[0]&255));
	    }else if (what==0x2f)
		return true;
	    else if (what==0x51)
		metaEvents.add(new TempoEvent(time,outTrackIndex,id,what,data));
	    else if (what==0x54)
		System.err.println("SMTPE Offset");
	    else if (what==0x58)
		metaEvents.add(new TimeSignatureEvent(time,outTrackIndex,id,what,data));
	    else if (what==0x59)
		metaEvents.add(new KeySignatureEvent(time,outTrackIndex,id,what,data));
	    else if (what==0x7f)
		System.err.println("Sequencer-Specific Meta-event");
	    return false;
	}
	private void expandTextEvent(TextEvent parent,Map<String,String>equalsMap,TextEvent te)throws IOException{
	    unused.add(parent);
	    if (equalsMap.containsKey(te.what))
		for (StringTokenizer st=new StringTokenizer(equalsMap.get(te.what),";"); st.hasMoreTokens();){
		    Matcher m=whatParamPattern.matcher(st.nextToken());
		    if (!m.matches())
			throw new IOException("Bad whatparam "+m);
		    expandTextEvent(parent,equalsMap,new TextEvent(parent,te.time,te.id,outTrackIndex,te.stop,te.trackName,te.matchStartTime,te.matchKey,m.group(1),makeParam(m.group(2))));
		}
	    else
		textEvents.computeIfAbsent(te.trackName,x->new ArrayList<TextEvent>()).add(te);
	}
	void read()throws IOException{
	    List<TextEvent>noend=new ArrayList<TextEvent>();
	    Map<String,String>equalsMap=new HashMap<String,String>();
	    int channel=-1;
	    for (;;){
		int deltatime=readVlen();
		time += deltatime;
		int one=dis.readByte()&255;
		String idc=id+','+(one&15);
		if (one==0xf0 || one==0xf7){
		    byte[]data=new byte[readVlen()];
		    dis.readFully(data);
		    throw new IOException(String.format("Sysex event 0x%02x len=%d",one,data.length));
		}else if (one==0xff){
		    int two=dis.readByte()&255;
		    byte[]data=new byte[readVlen()];
		    dis.readFully(data);
		    if (readMeta(two,data))
			break;
		}else if ((one&0xe0)==0x80){
		    if (channel!=-1 && channel!=(one&15))
			throw new IOException("More than one channel in track.");
		    channel = one&15;
		    int kk=dis.readByte()&255;
		    int vv=dis.readByte()&255;
		    if (keyTime[kk]!=-1){
			if (isTextTrack){
			    long start=keyTime[kk];
			    long stop=time;
			    String text=textMap.get(kk);
			    if (text==null)
				throw new RuntimeException("text==null kk="+kk+" trackName="+trackName+" time="+start);
			    Matcher m=textPattern.matcher(text);
			    if (!m.matches())
				throw new IOException("Bad text="+text);
			    String scope=m.group(1);
			    String keys=m.group(2);
			    String eq=m.group(3);
			    String wp=m.group(4);
			    if (eq!=null){
				equalsMap.put(eq,wp);
				wp = eq;
			    }
			    if (wp.indexOf(';')!=-1)
				throw new IOException(text);
			    m = whatParamPattern.matcher(wp);
			    if (!m.matches())
				throw new IOException("Bad text="+text);
			    String what=m.group(1);
			    String[]param=makeParam(m.group(2));
			    boolean matchStartTime=scope.indexOf('.')!=-1;
			    Set<Double>matchKey;
			    if (keys!=null){
				matchKey = new HashSet<Double>();
				for (StringTokenizer st=new StringTokenizer(keys,"|()"); st.hasMoreTokens();)
				    matchKey.add(note2Midi(st.nextToken()));
			    }else
				matchKey = null;
			    if (scope.indexOf('-')!=-1)
				start = stop;
			    if (scope.indexOf("!")!=-1)
				for (int i=noend.size();;){
				    TextEvent te=noend.get(--i);
				    if (te.what.equals(what)){
					noend.remove(i);
					te = new TextEvent(null,te.time,te.id,outTrackIndex,start,te.trackName,te.matchStartTime,te.matchKey,te.what,te.param);
					expandTextEvent(te,equalsMap,te);
					break;
				    }
				}
			    else{
				TextEvent te=new TextEvent(null,start,idc,outTrackIndex,stop,trackName,matchStartTime,matchKey,what,param);
				if (scope.indexOf(">")!=-1)
				    noend.add(te);
				else
				    expandTextEvent(te,equalsMap,te);
			    }
			}else
			    events.add(new NoteEvent(keyTime[kk],idc,outTrackIndex,time,trackName,kk,keyVelocity[kk],(one&15)==DRUMS_CHANNEL));
			keyTime[kk] = -1;
		    }
		    if ((one&0xf0)==0x90 && vv!=0){
			keyTime[kk] = time;
			keyVelocity[kk] = vv;
		    }
		}else if ((one&0xf0)==0xa0){
		    int kk=dis.readByte()&255;
		    int ww=dis.readByte()&255;
		    throw new IOException(String.format("Aftertouch channel=%d kk=0x%02x ww=0x%02x",one&15,kk,ww));
		}else if ((one&0xf0)==0xb0){
		    int cc=dis.readByte()&255;
		    int nn=dis.readByte()&255;
		    if (cc==7 && nn==100)
			;
		    else
			throw new IOException(String.format("MIDI Channel Mode channel=%d cc=0x%02x nn=0x%02x",one&15,cc,nn));
		}else if ((one&0xf0)==0xc0){
		    int program=dis.readByte()&255;
		    events.add(new ProgramChangeEvent(time,idc,outTrackIndex,program));
//		    System.err.println("ProgramChange channel="+(one&15)+" program="+program+" time="+time);
		}else if ((one&0xf0)==0xd0){
		    int ww=dis.readByte()&255;
		    throw new IOException(String.format("Channel pressure channel=%d %d",one&15,ww));
		}else if ((one&0xf0)==0xe0){
		    int lsb=dis.readByte()&255;
		    int msb=dis.readByte()&255;
		    int value=(int)(((msb<<7|lsb)-0x2000)*2/BEND_RANGE+.5+0x2000);
		    events.add(new BendEvent(time,idc,outTrackIndex,value));
		}else
		    throw new IOException(String.format("Weird 0x%02x",one));
	    }
	    for (int i=0; i<128; i++)
		if (keyTime[i]!=-1)
		    throw new IOException("Missing note stop");
	    for (TextEvent te:noend){
		te = new TextEvent(null,te.time,te.id,outTrackIndex,Long.MAX_VALUE,te.trackName,te.matchStartTime,te.matchKey,te.what,te.param);
		expandTextEvent(te,equalsMap,te);
	    }
	}
    }
    private class OutputEventMaker{
	private final OutputChannel[]outputChannels=new OutputChannel[16];
	private final Map<String,Integer>idToProgram=new HashMap<String,Integer>();
	private long worstAge=Long.MAX_VALUE;
	class SlideData{
	    Map<Long,List<NoteEvent>>map=new TreeMap<Long,List<NoteEvent>>();
	    OutputChannel oc;
	}
	private class OutputChannel{
	    final int number;
	    String lastId;
	    long lastEventTime=Integer.MIN_VALUE;
	    int lastProgram=-1;
	    private Set<Integer>alreadySetBendRange=new HashSet<Integer>();
	    OutputChannel(int number){
		this.number = number;
	    }
	    void add(long time,int sort,int outTrackIndex,int one,int...bytes){
		if (one==0xe0 && alreadySetBendRange.add(outTrackIndex))
		    addSetRpn(0,outTrackIndex,0,(int)(BEND_RANGE*128+.5));
		byte[]b=new byte[bytes.length];
		for (int i=0; i<bytes.length; i++)
		    b[i] = (byte)bytes[i];
		outputEvents.add(new OutputEvent(time,sort,outTrackIndex,one|number,b));
	    }
	    void addSetRpn(long time,int outTrackIndex,int n,int v){
		add(time,-100,outTrackIndex,0xb0,101,n>>7);
		add(time,-100,outTrackIndex,0xb0,100,n&127);
		add(time,-100,outTrackIndex,0xb0,6,v>>7);
		add(time,-100,outTrackIndex,0xb0,38,v&127);
		add(time,-100,outTrackIndex,0xb0,101,127);
		add(time,-100,outTrackIndex,0xb0,100,127);
	    }
	    void addNote(int program,NoteEvent note)throws IOException{
		addNotePart1(program,note);
		addNotePart2(note);
	    }
	    void addNotePart1(int program,NoteEvent note)throws IOException{
		lastId = note.id;
		lastEventTime = note.stop;
		if (program!=lastProgram){
		    lastProgram = program;
		    add(note.time,-100,note.outTrackIndex,0xb0,0,program>>14);
		    add(note.time,-100,note.outTrackIndex,0xb0,32,program>>7&127);
		    add(note.time,-100,note.outTrackIndex,0xc0,program&127);
		}
	    }
	    void addNotePart2(NoteEvent note)throws IOException{
		int ikey=(int)(note.key+.5);
		int bend=(int)(0x2000+.5+(note.key-ikey)*0x2000/BEND_RANGE);
		int v=Math.min((int)(note.velocity+.5),127);
		add(note.time,0,note.outTrackIndex,0x90,ikey,v);
		if (bend!=0x2000){
		    add(note.time,-10,note.outTrackIndex,0xe0,bend&127,bend>>7);
		    add(note.stop,-20,note.outTrackIndex,0xe0,0,0x40);
		}
		add(note.stop,-30,note.outTrackIndex,0x90,ikey,0);
	    }
	}
	private OutputChannel getOutputChannel(NoteEvent note){
	    if (note.percussion)
		return outputChannels[DRUMS_CHANNEL];
	    for (OutputChannel oc:outputChannels)
		if (oc.number!=DRUMS_CHANNEL && note.id.equals(oc.lastId))
		    return oc;
	    OutputChannel bestChannel=null;
	    for (OutputChannel oc:outputChannels)
		if (oc.number!=DRUMS_CHANNEL && (bestChannel==null || oc.lastEventTime<bestChannel.lastEventTime))
		    bestChannel = oc;
	    worstAge = Math.min(worstAge,note.time-bestChannel.lastEventTime);
	    return bestChannel;
	}
	private int parseProgram(String[]param){
	    int p=0;
	    for (int i=0; i<param.length; i++)
		p = p<<7|Integer.parseInt(param[i]);
	    return p;
	}
	private void addSlide(TextEvent te,OutputChannel oc,NoteEvent note,int program)throws IOException{
	    oc.addNotePart1(program,note);
	    SlideData sd=(SlideData)te.map.computeIfAbsent("slideData",x->new SlideData());
	    sd.oc = oc;
	    sd.map.computeIfAbsent(note.time,x->new ArrayList<NoteEvent>()).add(note);
	}
	private void finishSlide(TextEvent te)throws IOException{
	    SlideData sd=(SlideData)te.map.get("slideData");
	    if (sd==null)
		return;
	    double step=te.param.length>=1?Double.parseDouble(te.param[0]):0;
	    List<List<NoteEvent>>list=new ArrayList<List<NoteEvent>>(sd.map.values());
	    for (int i=0; i<list.size(); i++)
		Collections.sort(list.get(i),(x,y)->Double.compare(x.key,y.key));
	    boolean bad=false;
	    List<NoteEvent>l0=list.get(0);
	    for (int i=1; i<list.size(); i++){
		List<NoteEvent>li=list.get(i);
		if (li.size()!=l0.size())
		    bad = true;
		else if (step==0)
		    for (int j=0; j<l0.size(); j++)
			bad |= l0.get(j).key-l0.get(0).key!=li.get(j).key-li.get(0).key;
	    }
	    if (bad){
		for (List<NoteEvent>i:list)
		    for (NoteEvent j:i)
			sd.oc.addNotePart2(j);
		return;
	    }
	    if (step==0){
		long stop=0;
		for (List<NoteEvent>l:list)
		    stop = Math.max(stop,l.get(0).stop);
		for (NoteEvent n:list.get(0))
		    sd.oc.addNotePart2(new NoteEvent(n.time,n.id,n.outTrackIndex,stop,n.trackName,n.key,n.velocity,n.percussion));
		int lastBend=0x2000;
		NoteEvent n00=list.get(0).get(0);
		for (int i=1; i<list.size(); i++){
		    NoteEvent n0=list.get(i-1).get(0);
		    NoteEvent n1=list.get(i).get(0);
		    long time1=i==list.size()-1?stop:n1.time;
		    for (long j=n0.time; j<time1; j++){
			int bend=(int)(0x2000+0x1fff/BEND_RANGE*(j-n0.time)*(n1.key-n0.key)/(time1-n0.time));
			if (bend!=lastBend)
			    sd.oc.add(j,-20,n00.outTrackIndex,0xe0,bend&127,bend>>7);
			lastBend = bend;
		    }
		}
		sd.oc.add(stop,-20,n00.outTrackIndex,0xe0,0,0x40);
	    }else
		for (int i=1; i<list.size(); i++)
		    for (int j=0; j<list.get(0).size(); j++){
			NoteEvent n0=list.get(i-1).get(j);
			NoteEvent n1=list.get(i).get(j);
			long time1=i==list.size()-1?n1.stop:n1.time;
			int steps=(int)Math.ceil(Math.abs(n1.key-n0.key)/step)+1;
			for (int k=0; k<steps; k++){
			    long t=n0.time+(time1-n0.time)*k/steps;
			    long s=n0.time+(time1-n0.time)*(k+1)/steps;
			    double key=n0.key+k*(n1.key>n0.key?step:-step);
			    sd.oc.addNotePart2(new NoteEvent(t,n0.id,n0.outTrackIndex,s,n0.trackName,key,n0.velocity,n0.percussion));
			}
		    }
	}
	private void sendToOc(String id,long time,int sort,int outTrackIndex,int one,int...bytes){
	    for (OutputChannel oc:outputChannels)
		if (oc.lastId.equals(id)){
		    oc.add(time,sort,outTrackIndex,one,bytes);
		    break;
		}
	}
	void makeMetaEvents(long[]maxTime)throws IOException{
	    for (MetaEvent me:metaEvents){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		baos.write(me.what);
		writeVlen(baos,me.data.length);
		baos.write(me.data);
		outputEvents.add(new OutputEvent(me.time,-100,me.outTrackIndex,0xff,baos.toByteArray()));
		maxTime[0] = Math.max(maxTime[0],me.time);
	    }
	}
	void makeEvents(long[]maxTime)throws IOException{
	    Collections.sort(events);
	    for (int i=0; i<outputChannels.length; i++)
		outputChannels[i] = new OutputChannel(i);
	    for (Event e:events){
		maxTime[0] = Math.max(maxTime[0],e.time);
		if (e instanceof ProgramChangeEvent)
		    idToProgram.put(e.id,((ProgramChangeEvent)e).program);
		else if (e instanceof BendEvent){
		    int amount=((BendEvent)e).amount;
		    sendToOc(e.id,e.time,-20,e.outTrackIndex,0xe0,amount&127,amount>>7);
		}else{
		    NoteEvent note=(NoteEvent)e;
		    maxTime[0] = Math.max(maxTime[0],note.stop);
		    Map<String,TextEvent>temap=new HashMap<String,TextEvent>();
		    List<TextEvent>tes=textEvents.get(note.trackName);
		    if (tes!=null)
			for (TextEvent te:tes)
			    if (te.matches(note)){
				TextEvent old=temap.get(te.what);
				if (old==null || te.narrowerThan(old))
				    temap.put(te.what,te);
			    }
		    for (TextEvent te:temap.values())
			unused.remove(te.parent);
		    int program=idToProgram.getOrDefault(note.id,0);
		    TextEvent te;
		    if ((te=temap.remove("volume"))!=null)
			note.velocity *= Double.parseDouble(te.param[0]);
		    if ((te=temap.remove("vol"))!=null)
			note.velocity *= Double.parseDouble(te.param[0]);
		    if ((te=temap.remove("program"))!=null)
			program = parseProgram(te.param);
		    if ((te=temap.remove("changeNote"))!=null)
			note.key = note2Midi(te.param[0]);
		    if ((te=temap.remove("percussion"))!=null)
			note.percussion = true;
		    if ((te=temap.remove("melodic"))!=null)
			note.percussion = false;
		    OutputChannel oc=getOutputChannel(note);
		    if ((te=temap.remove("slide"))!=null){
			addSlide(te,oc,note,program);
			continue;
		    }
		    if ((te=temap.remove("bend"))!=null && !te.done){
			long x0=te.time;
			double y0=0x2000;
			int lastBend=0x2000;
			for (int i=0; i<te.param.length; i+=2){
			    long x1=te.time+(long)(Double.parseDouble(te.param[i])/60*(te.stop-te.time)+.5);
			    double y1=0x2000+0x1fff/BEND_RANGE*Double.parseDouble(te.param[i+1])/50;
			    for (long j=x0; j<x1; j++){
				int bend=(int)(y0+(j-x0)*(y1-y0)/(x1-x0));
				if (bend!=lastBend)
				    oc.add(j,-20,note.outTrackIndex,0xe0,bend&127,bend>>7);
				lastBend = bend;
			    }
			    x0 = x1;
			    y0 = y1;
			}
			oc.add(te.stop,-20,note.outTrackIndex,0xe0,0,0x40);
			te.done = true;
		    }
		    if ((te=temap.remove("collottava"))!=null){
			double shift=Double.parseDouble(te.param[0]);
			oc.addNote(program,new NoteEvent(note.time,note.id,note.outTrackIndex,note.stop,note.trackName,note.key+shift,note.velocity,note.percussion));
		    }
		    if ((te=temap.remove("crescendo"))!=null){
			double v0=Double.parseDouble(te.param[0]);
			double v1=Double.parseDouble(te.param[1]);
			note.velocity *= v0+(note.time-te.time)*(v1-v0)/(te.stop-te.time);
		    }
		    if (temap.size()!=0)
			throw new IOException("Bad What "+temap.keySet());
		    oc.addNote(program,note);
		}
	    }
	    for (List<TextEvent>list:textEvents.values())
		for (TextEvent te:list)
		    if (te.what.equals("slide"))
			finishSlide(te);
	    try (PrintStream ps=new PrintStream(new FileOutputStream("measureToTime"))){
		printMeasureToTime(ps,maxTime[0]);
	    }
	    System.err.println("WorstAge="+worstAge);
	    for (TextEvent te:unused)
		System.err.println("Unused "+te);
	}
    }
    private void makeOutputEvents()throws IOException{
	Collections.sort(metaEvents);
	OutputEventMaker oem=new OutputEventMaker();
	long[]maxTime=new long[1];
	oem.makeMetaEvents(maxTime);
	oem.makeEvents(maxTime);
	outputEvents.add(new OutputEvent(maxTime[0],100,-1,0xff,(byte)0x2f,(byte)0));
	Collections.sort(outputEvents);
    }
    private void output(DataOutputStream dos,int outTrackIndex,byte[]trackName)throws IOException{
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	baos.write(0);
	baos.write(255);
	baos.write(3);
	baos.write(trackName.length);
	baos.write(trackName);
	long lastTime=0;
	for (OutputEvent oe:outputEvents){
	    if (oe.outTrackIndex!=-1 && oe.outTrackIndex!=outTrackIndex)
		continue;
	    long delta=oe.time-lastTime;
	    lastTime = oe.time;
	    if (delta<0 || delta>=1<<28)
		throw new RuntimeException();
	    if (delta>=1<<7){
		if (delta>=1<<14){
		    if (delta>=1<<21)
			baos.write((int)delta>>21|128);
		    baos.write((int)delta>>14|128);
		}
		baos.write((int)delta>>7|128);
	    }
	    baos.write((int)delta&127);
	    baos.write(oe.one);
	    for (int i=0; i<oe.bytes.length; i++)
		baos.write(oe.bytes[i]);
	}
	byte[]b=baos.toByteArray();
	dos.write("MTrk".getBytes());
	dos.writeInt(b.length);
	dos.write(b);
    }
    private void readThd(DataInputStream dis)throws IOException{
	int format=dis.readShort();
	if (format!=0 && format!=1)
	    throw new IOException("Bad format="+format);
	int tracks=dis.readShort();
	int division=dis.readShort();
	if (division!=DIVISION)
	    throw new IOException("Bad division="+division);
    }
    private void read(DataInputStream dis,String id,int outTrackIndex)throws IOException{
	byte[]b=new byte[4];
	int trackNumber=0;
	for (;;){
	    try{
		dis.readFully(b,0,1);
	    }catch (EOFException e){
		return;
	    }
	    dis.readFully(b,1,3);
	    String type=new String(b,0,4);
	    int len=dis.readInt();
	    byte[]c=new byte[len];
	    dis.readFully(c);
	    if (type.equals("MThd"))
		try (DataInputStream d=new DataInputStream(new ByteArrayInputStream(c))){
		    readThd(d);
		}
	    else if (type.equals("MTrk"))
		try (DataInputStream d=new DataInputStream(new ByteArrayInputStream(c))){
		    new TrackReader(d,id+','+trackNumber++,outTrackIndex).read();
		}
	    else
		System.err.println("type="+type);
	}
    }
    public static void main(String[]argv)throws Exception{
	int j;
	for (j=0; j<argv.length; j++)
	    if (argv[j].equals("--fudge-lyrics"))
		fudgeLyricsStringTokenizer = new StringTokenizer(argv[++j],",");
	    else
		break;
	MergeMidi mm=new MergeMidi();
	for (int i=j; i<argv.length; i+=2)
	    try (DataInputStream dis=new DataInputStream(new FileInputStream(argv[i+1]))){
		mm.read(dis,argv[i+1],(i-j)/2);
	    }
	mm.makeOutputEvents();
	try (DataOutputStream dos=new DataOutputStream(System.out)){
	    dos.write("MThd".getBytes());
	    dos.writeInt(6);
	    dos.writeShort(1);
	    dos.writeShort((argv.length-j)/2);
	    dos.writeShort(DIVISION);
	    for (int i=j; i<argv.length; i+=2)
		mm.output(dos,(i-j)/2,argv[i].getBytes());
	}
    }
    private static class PrintInputStream extends FilterInputStream{
	PrintInputStream(InputStream in){
	    super(in);
	}
	@Override public int read()throws IOException{
	    int i=super.read();
	    if (i==-1)
		System.err.println("< EOF");
	    else
		System.err.println(String.format("< %02x",i));
	    return i;
	}
	@Override public int read(byte[]b,int off,int len)throws IOException{
	    int i=super.read(b,off,len);
	    if (i!=0){
		System.err.print("<");
		for (int j=0; j<i; j++)
		    System.err.print(String.format(" %02x",255&b[off+j]));
		System.err.println();
	    }
	    return i;
	}
    }
}
