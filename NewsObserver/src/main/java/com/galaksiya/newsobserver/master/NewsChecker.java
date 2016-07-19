package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

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

	private Hashtable<String, String> lastNews = new Hashtable<String, String>();
	
	private ArrayList<URL> RssLinksAl ;

	public NewsChecker(ArrayList<URL> RssLinksAl) {
		databaseFactory = DatabaseFactory.getInstance();
		dbForNews = databaseFactory.getDatabase("news");
		db = databaseFactory.getDatabase("STATISTICS");
		this.RssLinksAl=RssLinksAl;
	}

	public NewsChecker(Database dbObject) {
		db = dbObject;
	}

	public NewsChecker(String type, Database dbObject) {
		if (type.equalsIgnoreCase("test")) {
			this.dbForNews = dbObject;
			databaseFactory=DatabaseFactory.getInstance();
			this.db=databaseFactory.getDatabase("test");
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
		Hashtable<String, Integer> wordFrequencyPerNew = new Hashtable<String, Integer>();
		wordFrequencyPerNew = processOfWords.splitWords(message.getTitle() + " ", message.getDescription()); 
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
		boolean updateNew = true, updated = false;
		RssReader parserOfRss = new RssReader();
		ArrayList<FeedMessage> itemsAL = parserOfRss.parseFeed(rssURLs);
		if (itemsAL == null || itemsAL.isEmpty()) {
			LOG.error("There no news to handle.");
			return false;
		}
		for (FeedMessage message : itemsAL) {
			boolean isThereAnyNewNews = !message.getTitle().equals(lastNews.get(rssURLs.toString()));
			if (isThereAnyNewNews) { // if there is a new news we should insert
				if (updateNew) {
					lastNewsArray[0] = rssURLs.toString();
					lastNewsArray[1] = message.getTitle();
					updateNew = false;
					updated = true;
				}
				if (handleMessage(message) == null)
					continue;
			} else {
				break;
			}
		}
		if ( !updateNew && updated) {
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
		boolean proccessSuccessful = false,generalProcess=true;
		FrequencyUpdater updater = new FrequencyUpdater(db);
		Enumeration<String> e = wordFrequencyPerNew.keys();
		ArrayList<Document> didNotAdded = new ArrayList<Document>();
		while (e.hasMoreElements()) {
			String word = (String) e.nextElement();
			Integer frequency = wordFrequencyPerNew.get(word);
			proccessSuccessful = updater.addDatabase(datePerNew, word, frequency);
			if(!proccessSuccessful){
				didNotAdded.add(new Document().append("date", datePerNew).append("word", word).append("frequency", frequency));
				generalProcess=false;
			}
		}
		LOG.error("These are not added to database."+didNotAdded);
		return generalProcess;
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
			if (!lastNews.containsKey(rssURL.toString())) {
				lastNews.put(rssURL.toString(), "");
			}
			traverseNews(rssURL);
			LOG.debug(rssURL + " checked.");
		}
		return true;
	}
}
