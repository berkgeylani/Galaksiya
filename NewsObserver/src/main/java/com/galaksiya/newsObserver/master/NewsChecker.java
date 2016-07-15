package com.galaksiya.newsObserver.master;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsObserver.database.Database;
import com.galaksiya.newsObserver.database.DatabaseFactory;
import com.galaksiya.newsObserver.parser.FeedMessage;
import com.galaksiya.newsObserver.parser.RssReader;

public class NewsChecker {

	private final static Logger LOG = Logger.getLogger(NewsChecker.class);

	private Hashtable<String, String> lastNews = new Hashtable<String, String>();

	private Database db;

	private Database dbForNews;

	private DatabaseFactory databaseFactory;
	
	private ArrayList<URL> RssLinksAL ;

	public NewsChecker(String type, Database dbObject) {
		if (type.equalsIgnoreCase("test")) {
			this.dbForNews = dbObject;
			databaseFactory=DatabaseFactory.getInstance();
			this.db=databaseFactory.getDatabase("test");
		}
	}

	public NewsChecker(ArrayList<URL> RssLinksAL) {
		databaseFactory = DatabaseFactory.getInstance();
		dbForNews = databaseFactory.getDatabase("news");
		db = databaseFactory.getDatabase("STATISTICS");
		this.RssLinksAL=RssLinksAL;
	}

	public NewsChecker(Database dbObject) {
		db = dbObject;
	}

	/**
	 * It takes rss links and give one by one to travelInNews.
	 * 
	 * @param RssLinksAL
	 *            This arraylist is rss links list.
	 * @return true :success false :fail
	 */
	public boolean updateActualNews() {
		if (RssLinksAL == null || RssLinksAL.isEmpty())
			return false;

		for (URL rssURL : RssLinksAL) {
			if (!lastNews.containsKey(rssURL.toString())) {
				lastNews.put(rssURL.toString(), "");
			}
			traverseNews(rssURL);
			LOG.debug(rssURL + " checked.");
		}
		return true;
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
	public void traverseNews(URL rssURLs) {
		String[] lastNewsArray = new String[2];
		boolean updateNew = true, updated = false;
		RssReader parserOfRss = new RssReader();
		ArrayList<FeedMessage> itemsAL = parserOfRss.parseFeed(rssURLs);
		if (itemsAL == null || itemsAL.isEmpty()) {
			LOG.error("There no news to handle.");
			return;
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
			} else {// if we come the lately new we can break
				break;
			}
		}
		if (updateNew == false && updated == true) {
			lastNews.put(lastNewsArray[0], lastNewsArray[1]);
		}
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
		FeedMessage messageNews = processOfWords.cleanTextFields(message);
		if (dbForNews.exists(messageNews)) {
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
	 * It travel word by word and control the database has it already inserted.
	 * If yes,then increment it to database. If not,the insert it.
	 * 
	 * @param datePerNew
	 *            A hash table occurs message's pubdate.
	 * @param wordFrequencyPerNew
	 *            Contains word-frequency rows.
	 * @return true :Success false :fail
	 */
	private boolean traverseWordByWord(String datePerNew, Hashtable<String, Integer> wordFrequencyPerNew) {
		boolean proccessSuccessful = false;
		FrequencyUpdater updater = new FrequencyUpdater(db);
		Enumeration<String> e = wordFrequencyPerNew.keys();
		ArrayList<Document> didNotAdded = new ArrayList<Document>();
		while (e.hasMoreElements()) {
			String word = (String) e.nextElement();
			Integer frequency = wordFrequencyPerNew.get(word);
			proccessSuccessful = updater.addDatabase(datePerNew, word, frequency);
			if(!proccessSuccessful){
				didNotAdded.add(new Document().append("date", datePerNew).append("word", word).append("frequency", frequency));
			}
		}
		LOG.error("These are not added to database."+didNotAdded);
		return proccessSuccessful;
	}
}
