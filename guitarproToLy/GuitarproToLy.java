import java.io.*;
import java.util.*;

final class GuitarproToLy{
    public static void main(String[]argv)throws Exception{
	List<Arg>trackargs=new ArrayList<Arg>();
	Arg globalarg=Arg.getArgs(trackargs,argv);
	Gpfile gpfile=new Gp5file(new DataInputStream(System.in),globalarg);
	new Main(gpfile,globalarg,trackargs).make();
    }
}
