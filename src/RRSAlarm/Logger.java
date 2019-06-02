package RRSAlarm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
	
	private static Logger instance;
	  // Verhindere die Erzeugung des Objektes über andere Methoden
	  private Logger () {}
	  // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes 
	  // Objekt erzeugt und dieses zurückliefert.
	  public static Logger getInstance () {
	    if (Logger.instance == null) {
	    	Logger.instance = new Logger ();
	    }
	    return Logger.instance;
	  }
	  
    public void log(String message) { 
      PrintWriter out;
	try {
		out = new PrintWriter(new FileWriter("log.txt", true), true);
	      out.write(message);
	      System.out.print(message);
	      out.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    }
}
