package RRSAlarm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.html.parser.*; 

public class RRSAlarm {

	public static List<String> titlelist;
	public static RRSContainer container;
	public static boolean updatFailed = true;
	private static Logger log = Logger.getInstance();
	private static Firebase firebase;
	private static Map<String, String> firebaseContainer;
	private static HtmlManipulator html = new HtmlManipulator();
	private static PostgresDB postgre;

	public static void main(String[] args) {
		int whileLoop=0;
		PropertiesLoader prop = new PropertiesLoader();
		postgre = new PostgresDB(prop.get("DBURL"), prop.get("DBName"), prop.get("DBUser"), prop.get("DBPasswd"));
		postgre.init();
		
		titlelist = new ArrayList<String>();
		container = new RRSContainer();
		firebase = new Firebase();
		container.addDictionary();
		java.util.Calendar timer;
		firebaseContainer = new HashMap<>();
		
		try {
			firebase.connect("https://rrsalarm.firebaseio.com/", "../Credentials/firebase.json");
			container.initURLs();
			while (true) {
				container.avgUpdates = container.generalUpdates;
				container.generalUpdatesOld = container.generalUpdates;
				container.generalUpdates = 0;
				container.differentUpdates = 0;
				container.differentUpdatesOld = 0;

				container.cycle++;
				container.generalWordUpdate = 0;

				for (RRSURL url : container.urls) {
					container.listPosition = 1;
					container.CurrentURL = url.url;
					parseURL(url.url);
					if (container.differentUpdatesOld != container.generalUpdates) {
						container.differentUpdates++;
					}
					container.differentUpdatesOld = container.generalUpdates;
				}
				container.avgUpdates = (container.avgUpdates * 3 + container.generalUpdates) / 4;
				container.clearWordUpdates();
				timer = java.util.Calendar.getInstance();
				
				log.log("\nSleep... updates:" + container.generalUpdates + "   avg:" + container.avgUpdates
						+ "   different:" + container.differentUpdates + "  generalWordUpdate:"
						+ container.generalWordUpdate);
				// not for the for initial cycle
				if (whileLoop>0) {
					java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
					postgre.updateDB(sqlDate, "RSSAlaram/generalUpdates", container.generalUpdates+"");
					postgre.updateDB(sqlDate, "RSSAlaram/avgUpdates", container.avgUpdates+"");
					postgre.updateDB(sqlDate, "RSSAlaram/differentUpdates", container.differentUpdates+"");
					postgre.updateDB(sqlDate, "RSSAlaram/generalWordUpdate", container.generalWordUpdate+"");
					firebaseContainer.put("generalUpdates", ""+container.generalUpdates);
					firebaseContainer.put("avgUpdates", ""+container.avgUpdates);
					firebaseContainer.put("differentUpdates", ""+container.differentUpdates);
					firebaseContainer.put("generalWordUpdate", ""+container.generalWordUpdate);
					Firebase.setValue(firebaseContainer, "alarm/messages/"+timer.getTime().toString());
					firebaseContainer.clear();
					listTopTitle(timer);
				}

				timer.add(java.util.Calendar.getInstance().MINUTE, 5);
				for (int i = 0; i < 30; i++) {
					// 10 Seconds
					Thread.sleep(10000);
					log.log(".");
					if (java.util.Calendar.getInstance().after(timer)) {
						break;
					} else {
						if (updatFailed) {
							break;
						}
					}
				}
				log.log("\n");
				whileLoop++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		firebase.close();
		}
        
	}

	static void listTopTitle(java.util.Calendar timer) {
		Collections.sort(container.title);
		int count = 0;
		int top=0;
		int urlUpdate = 0;
		for (RRSTitle t : container.title) {
			if (count >= 3) {
				break;
			}
			if (t.cycle == container.cycle && t.listPosition <= 3) {
				String addstr = "";
				if (t.listPosition < 6) {
					addstr = "*2";
				}
				if (t.listPosition <= 3) {
					addstr = "*3";
				}
				for (RRSURL url : container.urls) {
					if (url.url.equals(t.URLs.get(0))) {
						urlUpdate = url.updates++;
					}
				}
				log.log("\n### " + t.listPosition + ". " + t.currentHit + addstr + " : " + t.timestamp.toLocaleString()
						+ "  " + t.title + " URLs.Count:" + t.URLs.size() + " URLs.Update:" + urlUpdate);
				if (count == 0) {
					java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
					postgre.updateDB(sqlDate, "RSSAlaram/currentHit", t.currentHit+"");
					postgre.updateDB(sqlDate, "RSSAlaram/listPosition", t.listPosition+"");
					postgre.updateDB(sqlDate, "RSSAlaram/URLCount", t.URLs.size()+"");
					postgre.updateDB(sqlDate, "RSSAlaram/URLUpdate", urlUpdate+"");
					postgre.updateDB(sqlDate, "RSSAlaram/Title", t.title);
				}
				firebaseContainer.put("position", ""+t.listPosition);
				firebaseContainer.put("currentHit", ""+t.currentHit);
				firebaseContainer.put("urlUpdate", ""+urlUpdate);
				firebaseContainer.put("title", ""+html.replaceHtmlEntities(t.title));
				firebaseContainer.put("topWords", ""+t.titleWordMatch);
				Firebase.setValue(firebaseContainer, "alarm/messages/"+timer.getTime().toString()+"/top/"+top++);
				firebaseContainer.clear();

				
				count++;
			}
		}
	}

	static List<String> findTitles(String textbody, int start, List<String> preTitle) {
		String titleStart = "<title>";
		String titleEnd = "</title>";
		String txt = textbody.substring(start, textbody.length());
		if (txt.indexOf(titleStart) > 0) {
			String title = txt.substring(txt.indexOf(titleStart) + titleStart.length(), txt.indexOf(titleEnd));
			int next = txt.indexOf(titleEnd) + titleEnd.length();
			if (title.contains("<![CDATA[")) {
				title = title.substring(9, title.length() - 3);
			}
			// System.out.println(title);
			preTitle.add(title);
			container.addTitle(html.clean(html.replaceHtmlEntities(title)));
			preTitle = findTitles(txt, next, preTitle);
		}
		return preTitle;
	}

	static void parseURL(String url) {
		HttpURLConnection con = null;
		try {

			URL myurl = new URL(url);
			con = (HttpURLConnection) myurl.openConnection();

			con.setRequestMethod("GET");

			StringBuilder content;

			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

				String line;

				content = new StringBuilder();

				while ((line = in.readLine()) != null) {
					content.append(line);
					content.append(System.lineSeparator());
				}

			}

			// System.out.println(content.toString());

			titlelist = findTitles(content.toString(), 0, titlelist);
			updatFailed = false;

		} catch (Exception e) {
			e.printStackTrace();
			updatFailed = true;
		} finally {
		}
		con.disconnect();
	}

}