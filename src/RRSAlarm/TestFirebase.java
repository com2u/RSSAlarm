package RRSAlarm;

import java.util.HashMap;

public class TestFirebase {
	
	private static Firebase firebase;
	

	public static void main(String[] args) {
		firebase = new Firebase();
		
		HashMap firebaseContainer = new HashMap<>();
		
		try {
			firebase.connect("https://rrsalarm.firebaseio.com/", "../Credentials/firebase.json");
			int i = 1;
			while (i==1) {
				
		
				
				String s = (String) (Firebase.getValue("alarmMessages/Message1")+"");
				System.out.println(s);
				firebaseContainer.clear();
				i++;
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		firebase.close();
		}


	}

}
