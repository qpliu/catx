import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

final class FindMeasuresInSvg{
    private static final double CLOSE=1e-4;
    private static final double LEFT_SPACE=1.7;
    private static final double SHORTEST_MEAUSRE=1;
    private static final Pattern matrixPattern=Pattern.compile("matrix\\(([-.0-9]+),([-.0-9]+),([-.0-9]+),([-.0-9]+),([-.0-9]+),([-.0-9]+)\\)");
    private static final Pattern linePattern=Pattern.compile("M\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*");
    private static final Pattern rectanglePattern=Pattern.compile("M\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*Z\\s*M\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*");
    private static final Pattern strokeWidthPattern=Pattern.compile(".*stroke-width:([0-9.]+);.*");
    String partpage;
    boolean verbose;
    double viewBoxLeft,viewBoxTop,viewBoxRight,viewBoxBottom;
    static class Matrix{
	final double m0,m1,m2,m3,m4,m5;
	Matrix(Matcher matcher){
	    m0 = Double.parseDouble(matcher.group(1));
	    m1 = Double.parseDouble(matcher.group(2));
	    m2 = Double.parseDouble(matcher.group(3));
	    m3 = Double.parseDouble(matcher.group(4));
	    m4 = Double.parseDouble(matcher.group(5));
	    m5 = Double.parseDouble(matcher.group(6));
	}
    }
    class Point{
	final double x,y;
	Point(Matcher matcher,int index,Matrix m){
	    double x=Double.parseDouble(matcher.group(index));
	    double y=Double.parseDouble(matcher.group(index+1));
	    this.x = (m.m0*x+m.m2*y+m.m4-viewBoxLeft)/(viewBoxRight-viewBoxLeft);
	    this.y = (m.m1*x+m.m3*y+m.m5-viewBoxTop)/(viewBoxBottom-viewBoxTop);
	}
    }
    static class HorizontalLine implements Comparable<HorizontalLine>{
	final double y,x0,x1,strokeWidth;
	HorizontalLine(double y,double x0,double x1,double strokeWidth){
	    this.y = y;
	    this.x0 = x0;
	    this.x1 = x1;
	    this.strokeWidth = strokeWidth;
	}
	@Override public int compareTo(HorizontalLine h){
	    return Double.compare(y,h.y);
	}
    }
    static class VerticalLine implements Comparable<VerticalLine>{
	final double x,y0,y1,strokeWidth;
	VerticalLine(double x,double y0,double y1,double strokeWidth){
	    this.x = x;
	    this.y0 = y0;
	    this.y1 = y1;
	    this.strokeWidth = strokeWidth;
	}
	@Override public int compareTo(VerticalLine v){
	    return Double.compare(x,v.x);
	}
    }
    final List<HorizontalLine>horizontalLines=new ArrayList<HorizontalLine>();
    final List<VerticalLine>verticalLines=new ArrayList<VerticalLine>();
    final List<double[]>rectangles=new ArrayList<double[]>();
    FindMeasuresInSvg(String[]argv)throws Exception{
	for (int i=0; i<argv.length; i++)
	    if (argv[i].equals("-v"))
		verbose = true;
	    else
		partpage = argv[i];
	SAXParserFactory.newInstance().newSAXParser().parse(System.in,new DefaultHandler(){
	    @Override public void startElement(String uri,String localName,String qName,Attributes attributes)throws SAXException{
		if (qName.equals("svg")){
		    StringTokenizer st=new StringTokenizer(attributes.getValue("viewBox"));
		    viewBoxLeft = Double.parseDouble(st.nextToken());
		    viewBoxTop = Double.parseDouble(st.nextToken());
		    viewBoxRight = Double.parseDouble(st.nextToken());
		    viewBoxBottom = Double.parseDouble(st.nextToken());
		}
		if (qName.equals("path")){
		    double strokeWidth=0;
		    Matcher m;
		    if ((m=strokeWidthPattern.matcher(String.valueOf(attributes.getValue("style")))).matches())
			strokeWidth = Double.parseDouble(m.group(1));
		    if ((m=matrixPattern.matcher(String.valueOf(attributes.getValue("transform")))).matches()){
			Matrix matrix=new Matrix(m);
			m = linePattern.matcher(String.valueOf(attributes.getValue("d")));
			if (m.matches())
			    gotLine(matrix,m,strokeWidth);
			m = rectanglePattern.matcher(String.valueOf(attributes.getValue("d")));
			if (m.matches())
			    gotRectangle(matrix,m);
		    }
		}
	    }
	});
	findMeasures();
    }
    void findMeasures(){
	Collections.sort(horizontalLines);
	Collections.sort(verticalLines);
	int measureNumber=0;
	double bigy=Double.NEGATIVE_INFINITY;
	for (int i=0; i<horizontalLines.size(); i++){
	    HorizontalLine hl=horizontalLines.get(i);
	    if (hl.y<bigy)
		continue;
	    List<HorizontalLine>list=new ArrayList<HorizontalLine>();
	    for (int j=i+1; j<horizontalLines.size()&&list.size()<4; j++){
		HorizontalLine hl2=horizontalLines.get(j);
		if (Math.abs(hl2.x0-hl.x0)<=CLOSE && Math.abs(hl2.x1-hl.x1)<=CLOSE)
		    list.add(hl2);
	    }
	    if (list.size()<4)
		continue;
	    double s1=0,s2=0;
	    for (int j=1; j<5; j++){
		HorizontalLine a=list.get(j-1);
		double b=(a.y-hl.y)*4/j;
		s1 += b;
		s2 += b*b;
	    }
	    double v=4*s2/s1/s1-1;
	    if (v>CLOSE)
		continue;
	    double y0=hl.y;
	    double y1=list.get(3).y;
	    double x0=hl.x0+LEFT_SPACE*(y1-y0);
	    for (VerticalLine vl:verticalLines)
		if (Math.abs(vl.y0-y0)<=CLOSE && Math.abs(vl.y1-y1)<=CLOSE){
		    double x1=vl.x;
		    if (x1-x0>SHORTEST_MEAUSRE*(y1-y0))
			System.out.println(partpage+" "+measureNumber+++" "+x0+" "+y0+" "+x1+" "+y1);
		    x0 = x1;
		}
	    if (hl.x1>=1-CLOSE && hl.x1-x0>SHORTEST_MEAUSRE*(y1-y0))
		System.out.println(partpage+" "+measureNumber+++" "+x0+" "+y0+" "+hl.x1+" "+y1);
	    for (double[]r:rectangles)
		if (Math.abs(r[1]-hl.y)<CLOSE && r[0]<=hl.x0 && hl.x0<=r[2])
		    bigy = Math.max(bigy,r[3]);
	}
    }
    void gotRectangle(Matrix matrix,Matcher matcher){
	Point p0=new Point(matcher,1,matrix);
	Point p1=new Point(matcher,3,matrix);
	Point p2=new Point(matcher,5,matrix);
	Point p3=new Point(matcher,7,matrix);
	if (p0.y==p1.y && p1.x==p2.x && p2.y==p3.y && p3.x==p0.x){
	    double x0=p0.x,y0=p0.y;
	    double x1=p2.x,y1=p2.y;
	    if (x0>x1){
		double t=x0;
		x0 = x1;
		x1 = t;
	    }
	    if (y0>y1){
		double t=y0;
		y0 = y1;
		y1 = t;
	    }
	if (verbose)
	    System.out.println("# R "+x0+" "+y0+" "+x1+" "+y1+" g="+matcher.group());
	    if (x1-x0<.01)
		rectangles.add(new double[]{x0,y0,x1,y1});
	}
    }
    void gotLine(Matrix matrix,Matcher matcher,double strokeWidth){
	Point p0=new Point(matcher,1,matrix);
	Point p1=new Point(matcher,3,matrix);
	double x0=p0.x,y0=p0.y;
	double x1=p1.x,y1=p1.y;
	if (x0>x1){
	    double t=x0;
	    x0 = x1;
	    x1 = t;
	}
	if (y0>y1){
	    double t=y0;
	    y0 = y1;
	    y1 = t;
	}
	if (y0==y1 && x0!=x1){
	    if (verbose)
		System.out.println("# H "+y0+" "+x0+" "+x1+" strokeWidth="+strokeWidth+" g="+matcher.group());
	    horizontalLines.add(new HorizontalLine(y0,x0,x1,strokeWidth));
	}
	if (x0==x1 && y0!=y1){
	    if (verbose)
		System.out.println("# V "+x0+" "+y0+" "+y1+" strokeWidth="+strokeWidth+" g="+matcher.group());
	    verticalLines.add(new VerticalLine(x0,y0,y1,strokeWidth));
	}
    }
    public static void main(String[]argv)throws Exception{
	new FindMeasuresInSvg(argv);
    }
}
