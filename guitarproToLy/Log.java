final class Log{
    static final int LEVEL_DEBUG=0;
    static final int LEVEL_INFO=1;
    static final int LEVEL_ERROR=2;
    static int level;
    static void log(int l,String fmt,Object...va){
	if (l>=level)
	    System.out.println(String.format(fmt,va));
    }
    static void debug(String fmt,Object...va){
	log(LEVEL_DEBUG,fmt,va);
    }
    static void info(String fmt,Object...va){
	log(LEVEL_INFO,fmt,va);
    }
    static void error(String fmt,Object...va){
	log(LEVEL_ERROR,fmt,va);
    }
}
