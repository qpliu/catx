import java.io.*;

class VKFileMaker extends LyricsKaraokeFileMaker{
    VKFileMaker(Main main,Arg arg,String suffix1,String suffix2)throws IOException{
	super(main,arg,suffix1,suffix2);
	addRehearsalMarks();
    }
    @Override void layout(MusicFileMaker mfm)throws IOException{
    }
    @Override void printMeasure(String measure){
	if (measure.endsWith(" |"))
	    measure = measure.substring(0,measure.length()-2);
	super.printMeasure(measure);
    }
    private void addRehearsalMarks(){
	boolean gotRepeat=false;
	for (Gpfile.Measure measure:main.gpfile.measures){
	    gotRepeat |= measure.repeatStart;
	    if (measure.rehearsalMark!=null)
		trackEvents.add(new Gpfile.LyricEvent(measure.time,new Rational(measure.time_n),"!mark="+measure.rehearsalMark,false,false,null));
	    if (!gotRepeat)
		trackEvents.add(new Gpfile.LyricEvent(measure.time,new Rational(measure.time_n),"!check="+measure.name,false,false,null));
	}
    }
}
