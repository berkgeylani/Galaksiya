package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsobserver.database.Database;
import com.galaksiya.newsobserver.database.DatabaseFactory;
import com.galaksiya.newsobserver.parser.FeedMessage;
import com.galaksiya.newsobserver.parser.RssReader;

public class NewsChecker {

	private final static Logger LOG = Logger.getLogger(NewsChecker.class);

	private DatabaseFactory databaseFactory;

	private Database db;

	private Database dbForNews;

	private Hashtable<String, String> lastNews = new Hashtable<>();

	private ArrayList<URL> RssLinksAl;

	public NewsChecker(ArrayList<URL> RssLinksAl) {
		databaseFactory = DatabaseFactory.getInstance();
		dbForNews = databaseFactory.getDatabase("news");
		db = databaseFactory.getDatabase("STATISTICS");
		this.RssLinksAl = RssLinksAl;
	}

	public NewsChecker(Database dbObject) {
		db = dbObject;
	}

	public NewsChecker(String type, Database dbObject) {
		if (type.equalsIgnoreCase("test")) {
			this.dbForNews = dbObject;
			databaseFactory = DatabaseFactory.getInstance();
			this.db = databaseFactory.getDatabase("test");
		}
	}

	/**
	 * It controls 'Is this new in rss link?'
	 * 
	 * @param title
	 *            A new of title
	 * @param rssURLs
	 *            A URL to read.
	 * @return true :Success false :fail
	 */
	public boolean containNewsTitle(String title, URL rssURLs) {
		RssReader parserOfRss = new RssReader();
		for (FeedMessage message : parserOfRss.parseFeed(rssURLs)) {
			if (message.getTitle().equals(title)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * It takes message and handle it to date-word-frequency.Then,It
	 * increment(update) or insert it.
	 * 
	 * @param message
	 *            It is only one new with title-description-pubdate.
	 * @return wordFrequencyPerNew It is a hash table which occurs
	 *         word-frequency
	 */
	public Hashtable<String, Integer> handleMessage(FeedMessage message) {
		WordProcessor processOfWords = new WordProcessor();
		DateUtils dateUtils = new DateUtils();
		String datePerNew = dateUtils.dateCustomize(message.getpubDate());
		if (dbForNews.exists(message)) {
			return null;
		}
		dbForNews.saveNews(message);
		Hashtable<String, Integer> wordFrequencyPerNew = processOfWords.splitWords(message.getTitle() + " ",
				message.getDescription());
		// wordFrequency test edecez
		if (!traverseWordByWord(datePerNew, wordFrequencyPerNew))
			return null;
		return wordFrequencyPerNew;
	}

	/**
	 * It travel news in url which is given with param. It gives news one by one
	 * to handleMessage. Also this function save the last news.
	 * 
	 * @param lastNews
	 *            It is hashtable which occurs rssLink-lastNew for this link.
	 * @param rssURLs
	 *            This is the url which will be read.
	 */
	public boolean traverseNews(URL rssURLs) {
		String[] lastNewsArray = new String[2];
		boolean updateNew = true;
		RssReader parserOfRss = new RssReader();
		ArrayList<FeedMessage> itemsAL = parserOfRss.parseFeed(rssURLs);
		if (itemsAL == null || itemsAL.isEmpty()) {
			LOG.error("There no news to handle.");
			return false;
		}
		for (FeedMessage message : itemsAL) {
			LOG.info("Haber Giriş\t" + (System.nanoTime() - Main.START_TIME) / 1000000000.0);
			boolean isThereAnyNewNews = !message.getTitle().equals(lastNews.get(rssURLs.toString()));
			if (isThereAnyNewNews) { // if there is a new news we should insert
				if (updateNew) {
					lastNewsArray[0] = rssURLs.toString();
					lastNewsArray[1] = message.getTitle();
					updateNew = false;
				}
				handleMessage(message);

			} else {
				break; // itemsAl yi size 0 yapılabilir.
			}
			LOG.info("haber Çıkış \t" + (System.nanoTime() - Main.START_TIME) / 1000000000.0);
		}
		if (!updateNew) {
			lastNews.put(lastNewsArray[0], lastNewsArray[1]);
		}
		return true;
	}

	/**
	 * It travel word by word and control the database has it already inserted.
	 * If yes,then increment it to database. If not,the insert it.
	 * 
	 * @param datePerNew
	 *            A hash table occurs message's pubdate.
	 * @param wordFrequencyPerNew
	 *            Contains word-frequency rows.
	 * @return true :Success false :fail
	 */
	public boolean traverseWordByWord(String datePerNew, Hashtable<String, Integer> wordFrequencyPerNew) {
		List<Document> documentList = new ArrayList<>();
		FrequencyUpdater updater = new FrequencyUpdater(db);
		DateUtils dateUtils = new DateUtils();
		Enumeration<String> e = wordFrequencyPerNew.keys();
		while (e.hasMoreElements()) {
			String word = (String) e.nextElement();
			Integer frequency = wordFrequencyPerNew.get(word);
			String customId = dateUtils.dateStrToHashForm(datePerNew) +"_" + word;
			documentList.add(new Document("_id",customId).append("date", dateUtils.dateConvert(datePerNew)).append("word", word)
					.append("frequency", frequency));
		}
		return updater.addDatabase(documentList);
	}

	/**
	 * It takes rss links and give one by one to travelInNews.
	 * 
	 * @param RssLinksAl
	 *            This arraylist is rss links list.
	 * @return true :success false :fail
	 */
	public boolean updateActualNews() {
		if (RssLinksAl == null || RssLinksAl.isEmpty())
			return false;
		for (URL rssURL : RssLinksAl) {
			LOG.info("link Giriş\t" + (System.nanoTime() - Main.START_TIME) / 1000000000.0);

			if (!lastNews.containsKey(rssURL.toString())) {
				lastNews.put(rssURL.toString(), "");
			}
			traverseNews(rssURL);
			LOG.info("link Çıkış \t" + (System.nanoTime() - Main.START_TIME) / 1000000000.0);
			LOG.debug(rssURL + " checked.");
		}
		return true;
	}
}
