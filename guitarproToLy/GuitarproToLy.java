import java.io.*;
import java.util.*;

final class GuitarproToLy{
    private static void usage(){
	System.err.println("Usage: java GuitarproToLy [global options] track n1 [track n1 options] track n2 [track n2 options] ... <gpfile");
	System.err.println("options:");
	System.err.println("[drumMap map]  Specify drum map.  Something like --drumMap \"49 cymc 0,38 sn 0,36 bd 1\"");
	System.err.println("[name partName]  Specify partName.");
	System.err.println("[no-ghost-notes]  Don't include ghost notes.");
	System.err.println("[no-string-numbers]  Don't include string numbers.");
	System.err.println("[only-highest-note]  Only include highest note of chords.");
	System.err.println("[only-lowest-note]  Only include lowest note of chords.");
	System.err.println("[output-tabs]  Output tabs instead of notes.");
	System.err.println("[output-text]  Output text.");
	System.err.println("[scale scale]  Specify note spelling.  Something like scale \"c des eisis\"");
	System.err.println("[shift n/d]  Shift notes right n/d beats.  Use shift -21/5 to shift notes left 4 1/5th beat.");
	System.err.println("[verbose level]");
	System.err.println("[who who]  Specify who.");
	System.exit(1);
    }
    public static void main(String[]argv)throws IOException{
	Gpfile gpfile=new Gp5file(new DataInputStream(System.in));
	Arg[]trackarg=new Arg[gpfile.tracks.length];
	Arg globalarg=new Arg();
	Arg arg=globalarg;
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("track")){
		int index=Integer.parseInt(argv[++i]);
		if (trackarg[index]==null)
		    trackarg[index] = globalarg.clone();
		arg = trackarg[index];
	    }else if (argv[i].equals("drumMap"))
		arg.drumMap = argv[++i];
	    else if (argv[i].equals("name"))
		arg.partName = argv[++i];
	    else if (argv[i].equals("no-ghost-notes"))
		arg.no_ghost_notes = true;
	    else if (argv[i].equals("no-string-numbers"))
		arg.no_string_numbers = true;
	    else if (argv[i].equals("only-highest-note"))
		arg.only_highest_note = true;
	    else if (argv[i].equals("only-lowest-note"))
		arg.only_lowest_note = true;
	    else if (argv[i].equals("output-tabs"))
		arg.output_tabs = true;
	    else if (argv[i].equals("output-text"))
		arg.output_text = true;
	    else if (argv[i].equals("scale")){
		for (StringTokenizer st=new StringTokenizer(argv[++i]); st.hasMoreTokens();){
		    String ly=st.nextToken();
		    arg.scale[Stuff.ly2midi(ly)%12] = ly;
		}
	    }else if (argv[i].equals("shift")){
		int j=argv[++i].indexOf('/');
		if (j==-1)
		    arg.shift = new Rational(Long.parseLong(argv[i]));
		else
		    arg.shift = new Rational(Long.parseLong(argv[i].substring(0,j)),Long.parseLong(argv[i].substring(j+1)));
	    }else if (argv[i].equals("verbose"))
		Log.level = Integer.parseInt(argv[++i]);
	    else if (argv[i].equals("who"))
		arg.who = argv[++i];
	    else
		usage();
	for (int i=0; i<trackarg.length; i++)
	    if (trackarg[i]==null)
		trackarg[i] = globalarg.clone();
	new Main(gpfile,globalarg,trackarg).make();
    }
}
