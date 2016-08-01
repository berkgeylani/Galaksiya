package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

	private final static Logger LOG = Logger.getLogger("com.newsobserver.admin");

	private static final Logger LOG_PERFORMANCE = Logger.getLogger("com.newsobserver.performance");

	private DatabaseFactory databaseFactory;

	private int countOfMessage = 0;

	private double controlOfRssFeeds = 0;

	private double processNewsTime = 0;

	private double processNonNewsTime = 0;

	private double UrlProcessTime = 0;

	private Double[] performanceLog = new Double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	private Database db;

	private Database dbForNews;

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
	 * @param rssURLs
	 *            This is the url which will be read.
	 */
	public boolean traverseNews(URL rssURLs) {
		countOfMessage = 1;
		RssReader parserOfRss = new RssReader();
		ArrayList<FeedMessage> itemsAL = parserOfRss.parseFeed(rssURLs);
		performanceLog[1] = System.currentTimeMillis() - UrlProcessTime;
		if (itemsAL == null || itemsAL.isEmpty()) {
			LOG.error("There is no news to handle.");
			return false;
		}
		// burada da diğeri belirlenecek
		for (FeedMessage message : itemsAL) {
			controlOfRssFeeds = System.currentTimeMillis();
			if (!dbForNews.exists(message)) {
				performanceLog[2] += System.currentTimeMillis() - controlOfRssFeeds;
				if (countOfMessage == 1) {
					processNewsTime = System.currentTimeMillis();
				}
				handleMessage(message);
				countOfMessage++;
			} else if(countOfMessage==1){
				processNewsTime = 0;
			}
		}
		if (performanceLog[2] == 0) {
			performanceLog[2] = System.currentTimeMillis() - controlOfRssFeeds;
		}
		if (processNewsTime != 0) {
			performanceLog[3] = System.currentTimeMillis() - processNewsTime;
		} else {
			performanceLog[3] = 0.0;
			processNonNewsTime = System.currentTimeMillis();
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
			String word = e.nextElement();
			Integer frequency = wordFrequencyPerNew.get(word);
			String customId = dateUtils.dateStrToHashForm(datePerNew) + "_" + word;
			documentList.add(new Document("_id", customId).append("date", dateUtils.dateConvert(datePerNew))
					.append("word", word).append("frequency", frequency));
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
		if (RssLinksAl == null || RssLinksAl.isEmpty()) {
			return false;
		}
		LOG_PERFORMANCE.debug(
				"RSS LINK TOPLAM SÜRE, RSS FEEDIN ÇEKILMESI, VAR MI YOK MU, HABERLERIN IŞLENMESI, VAR MI YOK MU / RSS HABER SAYISI, HABERLERIN IŞLENMESI / IŞLENEN HABER SAYISI");
		for (URL rssURL : RssLinksAl) {
			UrlProcessTime = System.currentTimeMillis();
			traverseNews(rssURL);
			performanceLog[0] = System.currentTimeMillis() - UrlProcessTime;
			performanceLog[4] = performanceLog[2] / countOfMessage;
			if (performanceLog[3] == 0) {
				performanceLog[3] = System.currentTimeMillis() - processNonNewsTime;
			}
			if(performanceLog[2] == 0)
				performanceLog[3]=0.0;
			performanceLog[5] = performanceLog[3] / countOfMessage;
			NumberFormat formatter = new DecimalFormat("#0.00");
			performanceLog[4] = Double.parseDouble(formatter.format(performanceLog[4]));
			performanceLog[5] = Double.parseDouble(formatter.format(performanceLog[5]));

			LOG_PERFORMANCE.debug(performanceLog[0] + "," + performanceLog[1] + "," + performanceLog[2] + ","
					+ performanceLog[3] + "," + performanceLog[4] + "," + performanceLog[5]);
			LOG.debug(rssURL + " checked.");
			performanceLog = new Double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		}
		LOG_PERFORMANCE.debug("bitttiii");
		return true;
	}
}
