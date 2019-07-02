package RRSAlarm;

import java.util.ArrayList;

public class RRSContainer {

	public String urlList[] = { "https://www.yahoo.com/news/rss/world", 
			"http://www.tagesschau.de/xml/atom/",
			"http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml",
			"http://www.spiegel.de/schlagzeilen/eilmeldungen/index.rss", 
			"http://rss.sueddeutsche.de/rss/Topthemen",
			"http://rss.sueddeutsche.de/rss/Eilmeldungen", 
			"http://www.spiegel.de/schlagzeilen/tops/index.rss",
			"https://www.welt.de/feeds/latest.rss", 
			"https://www.welt.de/feeds/topnews.rss",
			"http://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml", 
			"http://feeds.reuters.com/Reuters/worldNews",
			"https://www.presseportal.de/rss/presseportal.rss2",
			"https://rss.focus.de/fol/XML/rss_folnews_eilmeldungen.xml", 
			"https://www.t-online.de/themen/rss/",
			"http://rss.nytimes.com/services/xml/rss/nyt/World.xml",
			"http://rssfeeds.usatoday.com/usatoday-NewsTopStories", 
			"http://feeds.reuters.com/reuters/topNews",
			"https://www.stern.de/feed/standard/all/", "https://www.ad-hoc-news.de/rss/nachrichten.xml",
			"https://www.aljazeera.com/xml/rss/all.xml", "https://deutsch.rt.com/feeds/news/",
			"https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRGx1YlY4U0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US:en",
			//"https://www.reddit.com/r/worldnews/.rss",
			"http://www.globalissues.org/news/feed",
			"http://feeds.reuters.com/reuters/topNews",
			"http://feeds.feedburner.com/euronews/en/home/",
			"https://www.rt.com/rss/" };

	// public RRSTitle title = new RRSTitle();
	public ArrayList<RRSTitle> title = new ArrayList<RRSTitle>();
	public ArrayList<RRSWord> words = new ArrayList<RRSWord>();
	public ArrayList<RRSURL> urls = new ArrayList<RRSURL>();
	public int generalUpdates = 0;
	public int generalWordUpdate = 0;
	public int differentUpdates = 0;
	public int differentUpdatesOld = 0;
	public int generalUpdatesOld = 0;
	public RRSWord wordMostUsedThisCycle = new RRSWord();
	public RRSWord wordMostUsedAvg = new RRSWord();

	public int avgUpdates = 0;
	public int cycle = 0;
	public int listPosition = 1;
	public int globalURLupdates = 0;
	public String CurrentURL = "";
	private static Logger log = Logger.getInstance();

	void initURLs() {
		for (String urlText : urlList) {
			RRSURL url = new RRSURL();
			url.url = urlText;
			urls.add(url);
		}

	}

	void addTitle(String txt) {
		boolean found = false;
		RRSURL urlLocal = null;
		for (RRSTitle t : title) {
			if (t.title.equals(txt)) {
				t.count++;
				t.listPosition = this.listPosition;
				for (RRSURL url : urls) {
					if (url.url.equals(this.CurrentURL)) {
						urlLocal = url;
					}
				}

				// t.timestamp = new java.util.Date();
				if (cycle == 0) {

					log.log("\n" + t.listPosition + ". " + t.title + ":" + t.count + ":" + evaluateWords(txt) + ":"
							+ t.timestamp.toLocaleString() + " -- ");
				}
				found = true;
				break;
			}
		}
		if (!found) {
			RRSTitle t = new RRSTitle();
			t.title = txt;
			t.count = 1;
			t.timestamp = new java.util.Date();
			t.currentHit = evaluateWords(txt);
			t.listPosition = this.listPosition;
			t.URLs.add(CurrentURL);
			t.cycle = cycle;
			for (RRSURL url : urls) {
				if (url.url.equals(this.CurrentURL)) {
					urlLocal = url;
					url.updates++;
					globalURLupdates++;
					url.timestamp = new java.util.Date();
				}
			}
			title.add(t);
			generalUpdates++;
			String addstr = "";
			if (t.listPosition < 6) {
				addstr = "*2";
			}
			if (t.listPosition <= 3) {
				addstr = "*3";
			}
			t.titleWordMatch = addWords(t);
			log.log("\n" + t.listPosition + ". " + t.currentHit + addstr + " : " + t.timestamp.toLocaleString() + "  "
					+ t.title + " -- URLUpdates" + urlLocal.updates + " TitleWordMatch:" + t.titleWordMatch);

		}
		this.listPosition++;

	}

	int evaluateWords(String fullTitel) {
		int count = 0;
		int noOfWords = 0;
		String splitter = "\\s|,|\\.|-|:|!|\\?|;|\\(|\\)|\\+|\\&|\\=";
		for (String word : fullTitel.split(splitter)) {
			word = word.toLowerCase().trim().replaceAll(splitter,"");
			noOfWords++;
			for (RRSWord w : words) {
				if (w.word.equals(word)) {
					count += evaluateWordHit(w);
					break;
				}
			}
		}
		return count / noOfWords;
	}

	int evaluateWordHit(RRSWord w) {
		//return w.count * w.wordValue + w.updates * w.wordValue;
		return w.updates * w.wordValue;
	}

	void clearWordUpdates() {
		for (RRSWord w : words) {
			if (w.updatesThisCylce > 0) {
				w.updatesOld = 1;
				generalWordUpdate++;
			} else {
				w.updatesOld = 0;
			}
			w.titleWordMatchOld = w.titleWordMatch;
			w.topUpdatesOld = w.topUpdatesThisCylce;
			w.topUpdatesAvg = 0;
			w.titleWordMatch = 0;
			w.updatesThisCylce = 0;
			w.topUpdatesThisCylce = 0;
			if (w.updates > 0) {
				w.updates--;
			}
		}
		this.wordMostUsedThisCycle = new RRSWord();
		this.wordMostUsedAvg = new RRSWord();
	}

	int checkWordURLs(RRSWord w) {
		boolean found = false;
		for (String url : w.URLs) {
			if (url == CurrentURL) {
				found = true;
				return 0;
			}
		}
		if (found == false) {
			w.URLs.add(CurrentURL);
			w.differentURL++;
			return w.differentURL;
		}
		return 0;
	}

	RRSWord getWord(String word) {
		for (RRSWord w : words) {
			if (w.word.equals(word)) {
				return w;
			}
		}
		return null;
	}

	int addWords(RRSTitle titel) {
		int titleWordMatch = 0;
		int titleWordMatchValue = 0;
		RRSWord wordItem = null;
		String splitter = "\\s|,|\\.|-|:|!|\\?|;|\\(|\\)|\\+|\\&|\\=";
		for (String word : titel.title.split(splitter)) {
			word = word.toLowerCase().trim().replaceAll(splitter,"");
			// System.out.print("#");
			// System.out.print(word);
			boolean found = false;
			for (RRSWord w : words) {
				if (w.word.equals(word.toLowerCase())) {
					wordItem = w;
					w.count++;
					w.updates++;
					w.timestamp = new java.util.Date();
					w.updatesThisCylce++;
					if (titel.count < 3) {
						if (w.topUpdatesAvg < w.titleWordMatchOld) {
							w.topUpdatesAvg = w.titleWordMatchOld;
							w.topUpdatesAvg++;
						}
						w.topUpdatesThisCylce++;
					}
					titel.words.add(w);
					checkWordURLs(w);
					if (w.listPosition < 4) {
						titleWordMatchValue += w.wordValue;
						titleWordMatch++;
						w.titleWordMatch++;
					}
					if (listPosition <= w.listPosition) {
						w.listPosition = listPosition;
						w.listPositionTimestamp = new java.util.Date();
					}
					log.log(word + ":" + w.count + ":" + evaluateWordHit(w) + " ^:" + w.differentURL);
					found = true;
					break;
				}
			}
			if (!found) {
				RRSWord w = new RRSWord();
				wordItem = w;
				w.word = word.toLowerCase();
				w.count = 1;
				w.timestamp = new java.util.Date();
				w.wordValue = word.length();
				w.listPosition = listPosition;
				w.listPositionTimestamp = new java.util.Date();
				if (w.listPosition < 4) {
					w.titleWordMatch++;
				}
				checkWordURLs(w);
				if (!word.equals(word.toLowerCase())) {
					w.wordValue += 4;
				}
				if (w.wordValue < 4) {
					w.wordValue = 0;
				}
				words.add(w);
			}
		}
		for (RRSWord w : titel.words) {
			if (w.wordValue > 2) {
				if (this.wordMostUsedThisCycle.updatesThisCylce < w.updatesThisCylce) {
					this.wordMostUsedThisCycle = w;
				}
				if (titel.wordMostupdatesThisCylce.updatesThisCylce < w.updatesThisCylce) {
					titel.wordMostupdatesThisCylce = w;
				}
				if (titel.wordMostupdatesAgv.topUpdatesAvg < w.topUpdatesAvg) {
					titel.wordMostupdatesAgv = w;
				}
				if (this.wordMostUsedAvg.topUpdatesAvg < w.topUpdatesAvg) {
					this.wordMostUsedAvg = w;
				}
			}
		}
		if (titleWordMatch > 1) {
			log.log("  titleWordMatchValue:" + titleWordMatchValue + "   titleWordMatch:" + titleWordMatch
					+ "   topWordMatch:" + (wordItem.titleWordMatch + wordItem.titleWordMatchOld));
		}
		log.log(".fine");
		return titleWordMatch;
	}

	void addDictionary() {
		String lowWords[] = { "news", "Daily", "von", "mit", "RSS", "Feed", "www.ad-hoc-news.de", "Pressemeldung",
				"Polizeipr√§sidium", "Landespolizeiinspektion", "Ruhest√∂rung", "Polizeiinspektion", "Polizeidirektion",
				"Bundesstra√üe", "Autounfall", "LPI-G", "LPI-GTH", "BERLINER", "MORGENPOST", "Leitartikel",
				"Presseportal.de", "Rheinische", "Mitteldeutsche", "Zeitung", "Nachrichten", "SPIEGEL", "ONLINE", "BBC",
				"tagesschau", "tagesschau.de", "Themen-Ticker", "FOCUS-Online-Eilmeldungen", "News-Ticker" };
		for (String word : lowWords) {
			RRSWord w = new RRSWord();
			w.word = word.toLowerCase();
			w.count = 1;
			w.timestamp = new java.util.Date();
			w.wordValue = 0;
			words.add(w);
		}
		String highWords[] = { "Atom", "Global", "global", "Eilmeldung", "breaking", "Earth", "Weltweit", "Katastrophe",
				"Tote", "War", "Krieg", "Atomic", "Attac", "Blackout", "Bevˆlkerung", "Weltkrieg", "Bombe", "Fallout",
				"tsunami", "Asteroid", "Einschlag", "Impact", "Erdbeben", "Earthquake", "Terror", "Amok", "Amoklauf",
				"gezielt", "Explosion", "Notstand", "Rettungskr‰fte", "Panik", "Unruhen", "Giftgas", "Radioaktivit‰t" };
		for (String word : highWords) {
			RRSWord w = new RRSWord();
			w.word = word.toLowerCase();
			w.count = 1;
			w.timestamp = new java.util.Date();
			w.wordValue = 90;
			words.add(w);
		}
	}

}
