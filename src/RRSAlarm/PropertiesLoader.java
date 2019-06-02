package RRSAlarm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

public class PropertiesLoader {

     private static java.util.HashMap<String, String> settings;

	 PropertiesLoader(){
			java.util.Properties prop = new java.util.Properties();
			try {
				prop.load(new FileInputStream("../Credentials/com2u.properties"));	
				settings = new java.util.HashMap<String, String>();
				@SuppressWarnings("unchecked")
				Enumeration<String> enums = (Enumeration<String>) prop.propertyNames();
				while (enums.hasMoreElements()) {
				  String key = enums.nextElement();
				  String value = prop.getProperty(key);
				  System.out.println(key + " : " + value);
				  settings.put(key, value);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	 
	 String get(String key) {
		 String value = "";
		 if (settings.get(key) != null) {
			 value = settings.get(key);
		 }
		 return value;
	 }
	
}
