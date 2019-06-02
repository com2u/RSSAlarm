package RRSAlarm;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.CountDownLatch;

public class Firebase {
	private static final String DATABASE_URL = "https://rrsalarm.firebaseio.com/"; // my database @ google firebase
	private static final String CREDENTIALS_JSON = "Credentials/firebase.json"; //local json file with credentials
	public static FirebaseDatabase firebase;

	public static void main(String[] args) throws Exception {
		connect(DATABASE_URL, CREDENTIALS_JSON);

		Map<String, String> data = new HashMap<>();
		data.put("Enty1", "This is a new entry");
		data.put("Enty2", "This is another entry");
		setValue(data, "List/Items");

		update("Hello World", "alarmMessages/Message1");

		close();
		System.out.println("end");

	}

	public static void setValue(Object obj, String reference) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(reference);
		ref.setValueAsync(obj);
	}
	public static Object getValue(String reference) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(reference);
		return ref;
	}

	public static void connect(String firebaseDatabaseURL, String pathToCredentialsFile) throws FileNotFoundException, IOException {
		FileInputStream serviceAccount = new FileInputStream(pathToCredentialsFile);
		System.out.println(serviceAccount.toString());

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl(firebaseDatabaseURL).build();

		FirebaseApp.initializeApp(options);
		firebase = FirebaseDatabase.getInstance(firebaseDatabaseURL);
	}

	public static void update(Object value, String key) {
		try {
			DatabaseReference ref = firebase.getReference(key);
			final CountDownLatch latch = new CountDownLatch(1);
			ref.setValue(value, new DatabaseReference.CompletionListener() {
				@Override
				public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
					if (databaseError != null) {
						System.out.println("Data could not be saved " + databaseError.getMessage());
						latch.countDown();
					} else {
						System.out.println("Data saved successfully.");
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		firebase.getApp().delete();
	}

}
