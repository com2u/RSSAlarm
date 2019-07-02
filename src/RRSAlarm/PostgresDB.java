package RRSAlarm;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Properties;

public class PostgresDB {
	
	static PreparedStatement stmt = null;
	Calendar cal = Calendar.getInstance();  
	static java.sql.Timestamp sqlDate = null;
	static Connection conn = null;
	 String database="";
	String user="";
	String passwd="";
	
	
	public PostgresDB(String dbURL, String _database, String _user, String _passwd) {
		database = _database; 
		user = _user; 
		passwd = _passwd;
		try {		
			DriverManager.registerDriver(new org.postgresql.Driver());
			String dbpath = dbURL+database;
			//String dbpath = "jdbc:postgresql://localhost:5432/"+database;
			Properties parameters = new Properties();
			parameters.put("user", user);
			parameters.put("password", passwd);
			conn = DriverManager.getConnection(dbpath, parameters);
			 if (conn != null) {
	                System.out.println("Connected to database ");
	            }
			 else {
				 System.out.println("Connection lost");
				 return;
			 }
			
	                init();
	         
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	
	public void setup(String _database, String _user, String _passwd) {
		database = _database; 
		user = _user; 
		passwd = _passwd;
	}
	
	public void init(){
		try {		
			DriverManager.registerDriver(new org.postgresql.Driver());
			String dbURL = "jdbc:postgresql://localhost:5432/"+database;
			Properties parameters = new Properties();
			parameters.put("user", user);
			parameters.put("password", passwd);
			conn = DriverManager.getConnection(dbURL, parameters);
			 if (conn != null) {
	                System.out.println("Connected to database ");
	            }
			 else {
				 System.out.println("Connection lost");
				 return;
			 }
			 createNewTable();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public static void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void createNewTable() {

		// SQL statement for creating a new table
		String sqlDataset = "CREATE TABLE IF NOT EXISTS dataset (\n" 
				+ "	eventtime TIMESTAMP,\n" + "	topic text,\n"
				+ "	content text\n" + ");";
		System.out.println("Create Tabel dataset");
		try (Statement stmt = conn.createStatement()) {
			// create a new table
			stmt.execute(sqlDataset);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	
	public static void createStatusTable() {

		// SQL statement for creating a new table
		String sqlDataset = "CREATE TABLE IF NOT EXISTS status (\n" 
				+ "	eventtime TIMESTAMP,\n" + "	topic text,\n"
				+ "	content text\n" + ");";
		System.out.println("Create Tabel dataset");
		try (Statement stmt = conn.createStatement()) {
			// create a new table
			stmt.execute(sqlDataset);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	
	public void updateDBSequence(java.sql.Timestamp time, String topic, String content){
		String path = topic.substring(0,topic.lastIndexOf("/")+1);
		String header = topic.substring(topic.lastIndexOf("/")+1,topic.length());
		String[] subHeader = header.split(";");
		String[] subContent = content.split(";");
		System.out.println(subHeader);
		for (int i=0; i<subHeader.length; i++) {
			updateDB(time,path+subHeader[i],subContent[i]);
			 System.out.println(path+subHeader[i]+"="+subContent[i]);
	      }
	}
	
	public void updateDB(java.sql.Timestamp time, String topic,  String content){
		try {
			 if (conn == null) {
	                init();
	            }
			 if (conn == null) {
				 System.out.println("Connection lost");
				 return;
			 }
			 //sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
			 String sql = "INSERT INTO dataset (eventtime,topic,content) VALUES (?,?,?);";
			 stmt = conn.prepareStatement(sql);
			 stmt.setTimestamp(1, time);
			 String path = topic.substring(0,topic.lastIndexOf("\\")+1);
		     String sensor = topic.substring(topic.lastIndexOf("\\")+1,topic.length());
			 stmt.setString(2, topic);
			 //stmt.setString(3, sensor);
			 stmt.setString(3, content);
			 stmt.executeUpdate();
			 System.out.println("Updated dataset: "+topic+" : "+content);
	         
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			init();
		}
	}
	
	public void updateStatusDB(java.sql.Timestamp time, String topic,  String content){
		try {
			 if (conn == null) {
	                init();
	            }
			 if (conn == null) {
				 System.out.println("Connection lost");
				 return;
			 }
			 //sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
			 String sql = "UPDATE status (eventtime,topic,content) VALUES (?,?,?) WHERE topic = "+topic+";";
			 stmt = conn.prepareStatement(sql);
			 stmt.setTimestamp(1, time);
			 String path = topic.substring(0,topic.lastIndexOf("\\")+1);
		     String sensor = topic.substring(topic.lastIndexOf("\\")+1,topic.length());
			 stmt.setString(2, topic);
			 //stmt.setString(3, sensor);
			 stmt.setString(3, content);
			 stmt.executeUpdate();
			 System.out.println("Updated dataset: "+topic+" : "+content);
	         
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			init();
		}
	}
}
