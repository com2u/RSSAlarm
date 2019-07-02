package RRSAlarm;
import java.util.ArrayList;

public class RRSTitle implements Comparable <RRSTitle>{
	String title="";
	int count=0;
	int updates=0;
	java.util.Date timestamp;
	int currentHit=0;
	int listPosition=-1;
	int cycle=0;
    int titleWordMatch=0;
    int topURLUpdate=0;
    RRSWord wordMostupdatesThisCylce = new RRSWord();
    RRSWord wordMostupdatesAgv = new RRSWord();
    public ArrayList<String> URLs = new ArrayList<String>();
    public ArrayList<RRSWord> words = new ArrayList<RRSWord>();

    

	public int compareTo(RRSTitle t) {
		
			return t.currentHit-currentHit;
		

    }

}
