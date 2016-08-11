package com.galaksiya.newsobserver.database;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.galaksiya.newsobserver.master.DateUtils;
import com.galaksiya.newsobserver.parser.FeedMessage;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
/**
 * It is a database class which is using Derby Db.
 * @author francium
 *
 */
public class MongoDb implements Database {

	private static final int CONNECTION_POOL_SIZE = 10;

	private static final String LOCALHOST = "localhost";

	private static final Logger LOG = Logger.getLogger(MongoDb.class);

	private static final String MONGO_DB_NAME = "mydb";

	private static MongoClient mongoClient_INSTANCE;

	/**
	 * Fabric of a MongoClient
	 * 
	 * @return a MongoClient which parameters are "localhost",27017
	 *         (dbAdress,port).
	 */
	public static MongoClient getInstance() {
		if (mongoClient_INSTANCE == null) {
			synchronized (MongoClient.class) {
				if (mongoClient_INSTANCE == null) { // yes double check
					MongoClientOptions.Builder builder = new MongoClientOptions.Builder()
							.connectionsPerHost(CONNECTION_POOL_SIZE);
					mongoClient_INSTANCE = new MongoClient(new ServerAddress(LOCALHOST), builder.build());
				}
			}
		}
		return mongoClient_INSTANCE;
	}

	private String collectionName = DatabaseConstants.TABLE_NAME_STATISTICS;

	private DateUtils dateUtils = new DateUtils();

	/**
	 * It provide us to select collection name.
	 * 
	 * @param collectionName
	 *            String to set collection name.
	 */
	public MongoDb(String collectionName) {
		if (collectionName != null) {
			this.collectionName = collectionName;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.galaksiya.newsObserver.database.Database#contain(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public long contain(Date date, String word) {
		if (date==null ||word == null || word.equals("")) {
			return -1;
		}
		String customId = dateUtils.dateStrToHashForm(dateUtils.dateCustomize(date.toString())) + "_" + word;
		if (customId== null) {
			return 0;
		}
		Bson filter = new Document().append("_id", customId);

		MongoClient mongoClient = getInstance();
		return getCollection(mongoClient).count(filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#delete()
	 */
	@Override
	public boolean delete() {// Delete All documents from collection :Using
		// blank BasicDBObject
		MongoClient mongoClient = getInstance();
		try {
			getCollection(mongoClient).deleteMany(new Document());
			LOG.info("All data deleted");
			return true;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be deleted.", e);
		}
		return false;
	}

	@Override
	public boolean exists(FeedMessage message) {
		if (!isMessageCorrect(message)) {
			return false;
		}
		long count = 0;
		try {
			Bson filter = new Document().append("_id", hashString(message.getLink()));
			MongoClient mongoClient = getInstance();
			count = getCollection(mongoClient).count(filter);
			if (count > 0) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("News find process have a troble.", e);
		}
		return false;
	}

	/**
	 * It encodes a string to md5 string.
	 * 
	 * @param messageLink
	 *            In coming String to hash to md5 format
	 * @return Hashed
	 */
	private String hashString(String messageLink) {
		String hashedString = null;
		try {
			java.security.MessageDigest mDigest = java.security.MessageDigest.getInstance("MD5");
			byte[] array = mDigest.digest(messageLink.getBytes("UTF-8"));
			StringBuffer sBuffer = new StringBuffer();
			for (int i = 0; i < array.length; i++) {
				sBuffer.append(String.format("%02x", array[i]));
			}
			hashedString = sBuffer.toString();
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e1) {
			System.err.println("Can't hash to md5 please control your hasher function.");
		}
		return hashedString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch()
	 */
	@Override
	public List<Document> fetch() { // frequency sorted for all the words
		return fetch(new Document(), new Document().append("frequency", 1), -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch(Document
	 * find,Document sort,int limit)
	 */
	@Override
	public List<Document> fetch(Document find, Document sort, int limit) {
		MongoClient mongoClient = getInstance();
		try {
			FindIterable<Document> iterable = getCollection(mongoClient).find(find).sort(sort);
			if (limit >= 0)
				iterable = iterable.limit(limit);
			return findIterableToArraylist(iterable);
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch(java.lang.String)
	 */
	@Override
	public List<Document> fetch(String date) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", 1), -1);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch(java.lang.String,
	 * int)
	 */
	@Override
	public List<Document> fetch(String date, int limit) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", -1), limit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetchFirstWDocument()
	 */
	@Override
	public List<String> fetchFirstWDocument() {
		MongoClient mongoClient = getInstance();
		try {
			ArrayList<String> date_word_freq = new ArrayList<>();
			FindIterable<Document> search = getCollection(mongoClient).find();
			if (search.first() == null) {
				return null;
			}
			date_word_freq.add(search.first().get("date").toString());
			date_word_freq.add(search.first().get("word").toString());
			date_word_freq.add(search.first().get("frequency").toString());
			return date_word_freq;
		} catch (Exception e) {
			LOG.error("Can't fetch the first document.", e);
		}
		return null;
	}

	/**
	 * It returns MongoCollection with selected collection name(Default:
	 * statistics).It set when initialize MongoDb.
	 * 
	 * @param client
	 *            Get a MongoClient which is set "localhost", 27017.
	 * @return collection : Default: initialize statistics collection. You can
	 *         set when create MongoDb(//here).
	 */
	public MongoCollection<Document> getCollection(MongoClient client) {
		MongoCollection<Document> collection = client.getDatabase(MONGO_DB_NAME).getCollection(this.collectionName);
		return collection;
	}

	@Override
	public List<Document> getNews() {
		MongoClient mongoClient = getInstance();
		try {
			FindIterable<Document> iterable = getCollection(mongoClient).find();
			List<Document> newsAl = findIterableToArraylist(iterable);
			return newsAl;
		}catch (DuplicateKeyException e) {
			LOG.error("This new has already inserted.");
		}
		catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#save(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public boolean save(String dateStr, String word, int frequency) {
		MongoClient mongoClient = getInstance();
		if (word == null || word.equals("")) {
			return false;
		}
		try {
			Date date = dateUtils.dateConvert(dateStr);
			String customId = dateStr + "_" + word;
			getCollection(mongoClient).insertOne(new Document().append("_id", customId).append("date", date)
					.append("word", word).append("frequency", frequency));
			return true;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.galaksiya.newsObserver.database.Database#saveMany(List<Document>)
	 */
	@Override
	public synchronized boolean saveMany(List<Document> insertList) {
		if (insertList == null || insertList.isEmpty()) {
			return false;
		}
		MongoClient mongoClient = getInstance();
		boolean isSuccessful = false;
		try {
			getCollection(mongoClient).insertMany(insertList);
			isSuccessful = true;
		} catch (MongoWriteException e) {
			LOG.error("Datas couldn't be inserted.", e);
		}
		return isSuccessful;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#saveNews(com.galaksiya.
	 * newsObserver.parser.FeedMessage)
	 */
	@Override
	public boolean saveNews(FeedMessage message) {
		MongoClient mongoClient = getInstance();
		if (!isMessageCorrect(message)) {
			return false;
		}
		try {
			Date date = dateUtils.dateConvert(dateUtils.dateCustomize(message.getpubDate()));
			getCollection(mongoClient).insertOne(new Document("_id", hashString(message.getLink()))
					.append("title", message.getTitle()).append("description", message.getDescription())
					.append("link", message.getLink()).append("pubDate", date));
			return true;
			
		}
		catch (DuplicateKeyException e) {
			LOG.error("This new has already inserted.");
		} 
		catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return false;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * It controls the message like is it null or is its content contain
	 * anything or null.
	 * 
	 * @param message
	 *            message(title description pubdate)
	 * @return true : Correct Message false: Incorrect message
	 */
	public boolean isMessageCorrect(FeedMessage message) {
		if (message == null) {
			return false;
		}
		boolean isMessagetitleInvalid = message.getTitle() == null || "".equals(message.getTitle());
		boolean isMessageDescInvalid = message.getDescription() == null || "".equals(message.getDescription());
		boolean isMessagePubDInvalid = message.getpubDate() == null || "".equals(message.getpubDate());
		if (isMessagetitleInvalid || isMessageDescInvalid || isMessagePubDInvalid) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#totalCount()
	 */
	@Override
	public long totalCount() {
		return getCollection(getInstance()).count(new Document());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.galaksiya.newsObserver.database.Database#update(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public boolean update(String dateStr, String word, int frequency) {
		MongoClient mongoClient = getInstance();
		if (word == null || word.equals("")) {
			return false;
		}
		String customId = dateUtils.dateStrToHashForm(dateUtils.dateCustomize(dateStr)) + "_" + word;

		try {
			UpdateResult result = getCollection(mongoClient).updateOne(new Document("_id", customId),
					new Document("$inc", new Document("frequency", frequency)));
			return result.wasAcknowledged();
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be updated.", e);
		}
		return false;
	}

	/**
	 * It gives count of documents in FindIterable which is param
	 * 
	 * @param iterable
	 *            It is a Mongo query response object.
	 * @return A int which is count of documents.
	 */
	private List<Document> findIterableToArraylist(FindIterable<Document> iterable) {
		ArrayList<Document> dataAL = new ArrayList<>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			dataAL.add(cursor.next());
		}
		return dataAL;
	}
}