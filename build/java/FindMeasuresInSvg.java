import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

final class FindMeasuresInSvg{
    private static final Pattern matrixPattern=Pattern.compile("matrix\\(([-.0-9]+),([-.0-9]+),([-.0-9]+),([-.0-9]+),([-.0-9]+),([-.0-9]+)\\)");
    private static final Pattern linePattern=Pattern.compile("M\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*");
    private static final Pattern rectanglePattern=Pattern.compile("M\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*L\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*Z\\s*M\\s*([-.0-9]+)\\s+([-.0-9]+)\\s*");
    final String partpage;
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
	final double y,x0,x1;
	HorizontalLine(double y,double x0,double x1){
	    this.y = y;
	    this.x0 = x0;
	    this.x1 = x1;
	}
	@Override public int compareTo(HorizontalLine h){
	    return Double.compare(y,h.y);
	}
	@Override public String toString(){
	    return "y="+y+" x0="+x0+" x1="+x1;
	}
    }
    static class VerticalLine implements Comparable<VerticalLine>{
	final double x,y0,y1;
	VerticalLine(double x,double y0,double y1){
	    this.x = x;
	    this.y0 = y0;
	    this.y1 = y1;
	}
	@Override public int compareTo(VerticalLine v){
	    return Double.compare(x,v.x);
	}
    }
    final Set<String>done=new HashSet<String>();
    final Queue<HorizontalLine>queue=new PriorityQueue<HorizontalLine>();
    final Map<String,List<HorizontalLine>>horizontalLines=new HashMap<String,List<HorizontalLine>>();
    final Map<String,List<VerticalLine>>verticalLines=new HashMap<String,List<VerticalLine>>();
    final Map<Double,List<double[]>>rectangles=new HashMap<Double,List<double[]>>();
    FindMeasuresInSvg(String partpage)throws Exception{
	this.partpage = partpage;
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
		    Matcher m=matrixPattern.matcher(String.valueOf(attributes.getValue("transform")));
		    if (m.matches()){
			Matrix matrix=new Matrix(m);
			m = linePattern.matcher(String.valueOf(attributes.getValue("d")));
			if (m.matches())
			    gotLine(matrix,m);
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
	for (List<HorizontalLine>l:horizontalLines.values()){
	    Collections.sort(l);
	    Collections.reverse(l);
	}
	for (List<VerticalLine>l:verticalLines.values())
	    Collections.sort(l);
	int measureNumber=0;
	double bigy=Double.NEGATIVE_INFINITY;
	for (HorizontalLine hl; (hl=queue.poll())!=null;){
	    List<HorizontalLine>l=horizontalLines.get(hl.x0+" "+hl.x1);
	    if (hl!=l.get(l.size()-1))
		throw new RuntimeException();
	    l.remove(l.size()-1);
	    if (l.size()>=4 && hl.y>bigy){
		double s1=0,s2=0;
		for (int i=1; i<5; i++){
		    HorizontalLine a=l.get(l.size()-i);
		    double b=(a.y-hl.y)*4/i;
		    s1 += b;
		    s2 += b*b;
		}
		double v=4*s2/s1/s1-1;
		if (v<1e-6){
		    double y0=hl.y;
		    double y1=l.get(l.size()-4).y;
		    List<VerticalLine>m=verticalLines.remove(y0+" "+y1);
		    if (m!=null){
			double x0=hl.x0;
			for (VerticalLine a:m){
			    double x1=a.x;
			    if (x1-x0>.5*(y1-y0))
				System.out.println(partpage+" "+measureNumber+++" "+x0+" "+y0+" "+x1+" "+y1);
			    x0 = x1;
			}
		    }
		    List<double[]>ll=rectangles.get(hl.y);
		    if (ll!=null)
			for (double[]r:ll)
			    if (r[0]<=hl.x0 && hl.x0<=r[2])
				bigy = Math.max(bigy,r[3]);
		}
	    }
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
	    rectangles.computeIfAbsent(y0,x->new ArrayList<double[]>()).add(new double[]{x0,y0,x1,y1});
	}
    }
    void gotLine(Matrix matrix,Matcher matcher){
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
	if (done.add(x0+" "+y0+" "+x1+" "+y1)){
	    if (y0==y1 && x0!=x1){
		HorizontalLine hl=new HorizontalLine(y0,x0,x1);
		queue.add(hl);
		horizontalLines.computeIfAbsent(x0+" "+x1,x->new ArrayList<HorizontalLine>()).add(hl);
	    }
	    if (x0==x1 && y0!=y1){
		VerticalLine vl=new VerticalLine(x0,y0,y1);
		verticalLines.computeIfAbsent(y0+" "+y1,x->new ArrayList<VerticalLine>()).add(vl);
	    }
	}
    }
    public static void main(String[]argv)throws Exception{
	new FindMeasuresInSvg(argv[0]);
    }
}
