package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsobserver.database.Database;
import com.galaksiya.newsobserver.database.DatabaseFactory;
import com.galaksiya.newsobserver.parser.FeedMessage;
import com.galaksiya.newsobserver.parser.RssReader;

public class NewsChecker implements Runnable {

	private final static Logger LOG = Logger.getLogger("com.newsobserver.admin");


	private DatabaseFactory databaseFactory;

	private BlockingQueue<Feed> sharedFeed;
	private BlockingQueue<URL> sharedURL;

	private Database db;

	private Database dbForNews;

	public NewsChecker(BlockingQueue<URL> sharedURLQueue,BlockingQueue<Feed> sharedFeedQueue) {
		databaseFactory = DatabaseFactory.getInstance();
		dbForNews = databaseFactory.getDatabase("news");
		db = databaseFactory.getDatabase("STATISTICS");
		this.sharedFeed = sharedFeedQueue;
		this.sharedURL = sharedURLQueue;
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
	 * @throws InterruptedException 
	 */
	public boolean traverseNews(Feed feed) throws InterruptedException {
		if (feed == null || feed.isEmpty()) {
			return false;
		}
		long time = System.currentTimeMillis();
		LOG.debug(": process started for this url :" + feed.getUrl());
		// buraya pojo classı gelicek
		BlockingQueue<FeedMessage> itemsAL = feed.getFeedMessages();
	
		if (itemsAL == null || itemsAL.isEmpty()) {
			LOG.error("There is no news to handle.");
			return false;
		}
		// burada da diğeri belirlenecek
		long handleMessageSure =System.currentTimeMillis();
		System.out.println(itemsAL.size());
		
		while(itemsAL.size()!=0){
			FeedMessage message = itemsAL.take();
			handleMessageSure= System.currentTimeMillis();
			if (!dbForNews.exists(message)) {
				handleMessage(message);
			}
			LOG.debug("handle message suresi \t"+(handleMessageSure-System.currentTimeMillis()));
		}
		LOG.debug("link başına\t"+(System.currentTimeMillis() - time));

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

	@Override
	public void run() {
		while (!sharedFeed.isEmpty() || !sharedURL.isEmpty()) {
			try {
				traverseNews(sharedFeed.take());
			} catch (InterruptedException e) {
				LOG.error("A Problem while taking from sharedFeed(BlockingQueue<Feed>)", e);
			}
		}
		return;
	}

}
