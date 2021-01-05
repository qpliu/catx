import java.util.*;

final class Stuff{
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
    static String quote(String s){
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
    static class MidiInstrument{
	final String name;
	boolean continuousSlide; 
	MidiInstrument(String name,boolean continuousSlide){
	    this.name = name;
	    this.continuousSlide = continuousSlide;
	}
    }
    private static final MidiInstrument[]midiInstrumentNames={
	new MidiInstrument("acoustic grand",false),
	new MidiInstrument("bright acoustic",false),
	new MidiInstrument("electric grand",false),
	new MidiInstrument("honky-tonk",false),
	new MidiInstrument("electric piano 1",false),
	new MidiInstrument("electric piano 2",false),
	new MidiInstrument("harpsichord",false),
	new MidiInstrument("clav",false),
	new MidiInstrument("celesta",false),
	new MidiInstrument("glockenspiel",false),
	new MidiInstrument("music box",false),
	new MidiInstrument("vibraphone",false),
	new MidiInstrument("marimba",false),
	new MidiInstrument("xylophone",false),
	new MidiInstrument("tubular bells",false),
	new MidiInstrument("dulcimer",false),
	new MidiInstrument("drawbar organ",false),
	new MidiInstrument("percussive organ",false),
	new MidiInstrument("rock organ",false),
	new MidiInstrument("church organ",false),
	new MidiInstrument("reed organ",false),
	new MidiInstrument("accordion",false),
	new MidiInstrument("harmonica",false),
	new MidiInstrument("concertina",false),
	new MidiInstrument("acoustic guitar (nylon)",true),
	new MidiInstrument("acoustic guitar (steel)",true),
	new MidiInstrument("electric guitar (jazz)",true),
	new MidiInstrument("electric guitar (clean)",true),
	new MidiInstrument("electric guitar (muted)",true),
	new MidiInstrument("overdriven guitar",true),
	new MidiInstrument("distorted guitar",true),
	new MidiInstrument("guitar harmonics",true),
	new MidiInstrument("acoustic bass",true),
	new MidiInstrument("electric bass (finger)",true),
	new MidiInstrument("electric bass (pick)",true),
	new MidiInstrument("fretless bass",true),
	new MidiInstrument("slap bass 1",true),
	new MidiInstrument("slap bass 2",true),
	new MidiInstrument("synth bass 1",true),
	new MidiInstrument("synth bass 2",true),
	new MidiInstrument("violin",true),
	new MidiInstrument("viola",true),
	new MidiInstrument("cello",true),
	new MidiInstrument("contrabass",true),
	new MidiInstrument("tremolo strings",true),
	new MidiInstrument("pizzicato strings",false),
	new MidiInstrument("orchestral harp",false),
	new MidiInstrument("timpani",false),
	new MidiInstrument("string ensemble 1",true),
	new MidiInstrument("string ensemble 2",true),
	new MidiInstrument("synthstrings 1",true),
	new MidiInstrument("synthstrings 2",true),
	new MidiInstrument("choir aahs",true),
	new MidiInstrument("voice oohs",true),
	new MidiInstrument("synth voice",true),
	new MidiInstrument("orchestra hit",false),
	new MidiInstrument("trumpet",false),
	new MidiInstrument("trombone",true),
	new MidiInstrument("tuba",false),
	new MidiInstrument("muted trumpet",false),
	new MidiInstrument("french horn",false),
	new MidiInstrument("brass section",false),
	new MidiInstrument("synthbrass 1",true),
	new MidiInstrument("synthbrass 2",true),
	new MidiInstrument("soprano sax",false),
	new MidiInstrument("alto sax",false),
	new MidiInstrument("tenor sax",false),
	new MidiInstrument("baritone sax",false),
	new MidiInstrument("oboe",false),
	new MidiInstrument("english horn",false),
	new MidiInstrument("bassoon",false),
	new MidiInstrument("clarinet",false),
	new MidiInstrument("piccolo",false),
	new MidiInstrument("flute",false),
	new MidiInstrument("recorder",false),
	new MidiInstrument("pan flute",false),
	new MidiInstrument("blown bottle",false),
	new MidiInstrument("shakuhachi",false),
	new MidiInstrument("whistle",false),
	new MidiInstrument("ocarina",false),
	new MidiInstrument("lead 1 (square)",true),
	new MidiInstrument("lead 2 (sawtooth)",true),
	new MidiInstrument("lead 3 (calliope)",true),
	new MidiInstrument("lead 4 (chiff)",true),
	new MidiInstrument("lead 5 (charang)",true),
	new MidiInstrument("lead 6 (voice)",true),
	new MidiInstrument("lead 7 (fifths)",true),
	new MidiInstrument("lead 8 (bass+lead)",true),
	new MidiInstrument("pad 1 (new age)",true),
	new MidiInstrument("pad 2 (warm)",true),
	new MidiInstrument("pad 3 (polysynth)",true),
	new MidiInstrument("pad 4 (choir)",true),
	new MidiInstrument("pad 5 (bowed)",true),
	new MidiInstrument("pad 6 (metallic)",true),
	new MidiInstrument("pad 7 (halo)",true),
	new MidiInstrument("pad 8 (sweep)",true),
	new MidiInstrument("fx 1 (rain)",true),
	new MidiInstrument("fx 2 (soundtrack)",true),
	new MidiInstrument("fx 3 (crystal)",true),
	new MidiInstrument("fx 4 (atmosphere)",true),
	new MidiInstrument("fx 5 (brightness)",true),
	new MidiInstrument("fx 6 (goblins)",true),
	new MidiInstrument("fx 7 (echoes)",true),
	new MidiInstrument("fx 8 (sci-fi)",true),
	new MidiInstrument("sitar",true),
	new MidiInstrument("banjo",true),
	new MidiInstrument("shamisen",true),
	new MidiInstrument("koto",true),
	new MidiInstrument("kalimba",false),
	new MidiInstrument("bagpipe",false),
	new MidiInstrument("fiddle",true),
	new MidiInstrument("shanai",false),
	new MidiInstrument("tinkle bell",false),
	new MidiInstrument("agogo",false),
	new MidiInstrument("steel drums",false),
	new MidiInstrument("woodblock",false),
	new MidiInstrument("taiko drum",false),
	new MidiInstrument("melodic tom",false),
	new MidiInstrument("synth drum",false),
	new MidiInstrument("reverse cymbal",false),
	new MidiInstrument("guitar fret noise",false),
	new MidiInstrument("breath noise",false),
	new MidiInstrument("seashore",false),
	new MidiInstrument("bird tweet",false),
	new MidiInstrument("telephone ring",false),
	new MidiInstrument("helicopter",false),
	new MidiInstrument("applause",false),
	new MidiInstrument("gunshot",false),
    };
    static boolean canDoContinuousSlide(int p){
	return p>=0&&p<midiInstrumentNames.length?midiInstrumentNames[p].continuousSlide:false;
    }
    static String midiInstrumentToString(int p){
	return p>=0&&p<midiInstrumentNames.length?midiInstrumentNames[p].name:null;
    }
}
