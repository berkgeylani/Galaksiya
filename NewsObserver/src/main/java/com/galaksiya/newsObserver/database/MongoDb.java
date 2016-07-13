package com.galaksiya.newsObserver.database;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.galaksiya.newsObserver.master.DateUtils;
import com.galaksiya.newsObserver.parser.FeedMessage;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoDb implements Database {

	static final Logger LOG = Logger.getLogger(MongoDb.class);

	private String collectionName;

	private static MongoClient mongoClient;

	/**
	 * Fabric of a MongoClient
	 * 
	 * @return a MongoClient which parameters are "localhost",27017
	 *         (dbAdress,port).
	 */
	public static MongoClient getInstance() {
		if (mongoClient == null) {
			synchronized (MongoClient.class) {
				if (mongoClient == null) { // yes double check
					MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
					builder.connectionsPerHost(100);
					mongoClient = new MongoClient(new ServerAddress("localhost"), builder.build());
				}
			}
		}
		return mongoClient;
	}

	private DateUtils dateUtils = new DateUtils();

	public MongoDb() {
	}

	/**
	 * It provide us to select collection name.
	 * 
	 * @param collectionName
	 *            String to set collection name.
	 */
	public MongoDb(String collectionName) {
		this.collectionName = collectionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.galaksiya.newsObserver.database.Database#contain(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public long contain(String dateStr, String word) {
		if (word == null || word.equals("")) {
			return -1;
		}
		Date date = dateUtils.dateConvert(dateStr);

		Bson filter = new Document().append("date", date).append("word", word);

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
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch()
	 */
	@Override
	public ArrayList<Document> fetch() { // frequency sorted for all the words
		return fetch(new Document(), new Document().append("frequency", 1), -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch(java.lang.String)
	 */
	@Override
	public ArrayList<Document> fetch(String date) {
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
	public ArrayList<Document> fetch(String date, int limit) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", -1), limit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetchFirstWDocument()
	 */
	@Override
	public ArrayList<String> fetchFirstWDocument() {
		MongoClient mongoClient = getInstance();
		try {
			ArrayList<String> date_word_freq = new ArrayList<String>();
			FindIterable<Document> search = getCollection(mongoClient).find();
			if (search.first() == null) {
				return null;
			}
			date_word_freq.add(search.first().get("date").toString());
			date_word_freq.add(search.first().get("word").toString());
			date_word_freq.add(search.first().get("frequency").toString());

			return date_word_freq;
		} catch (Exception e) {
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
		MongoDatabase mDatabase = client.getDatabase("mydb");
		MongoCollection<Document> collection = null;
		if (this.collectionName == null) {
			collection = mDatabase.getCollection("statistics");
		} else {
			collection = mDatabase.getCollection(this.collectionName);
		}
		return collection;
	}

	/**
	 * It gives count of documents in FindIterable which is param
	 * @param iterable It is a Mongo query response object.
	 * @return A int which is count of documents.
	 */
	public ArrayList<Document> findIterableToArraylist(FindIterable<Document> iterable) {
		ArrayList<Document> dataAL = new ArrayList<Document>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			dataAL.add(cursor.next());
		}
		return dataAL;
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
			getCollection(mongoClient)
					.insertOne(new Document().append("date", date).append("word", word).append("frequency", frequency));
			return true;

		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);

		}
		return false;
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
		if (message == null || message.getTitle() == null || message.getTitle().equals("")
				|| message.getDescription() == null || message.getDescription().equals("")
				|| message.getpubDate() == null || message.getpubDate().equals("")) {
			return false;
		}
		try {
			Date date = dateUtils.dateConvert(dateUtils.dateCustomize(message.getpubDate()));
			getCollection(mongoClient).insertOne(new Document().append("title", message.getTitle())
					.append("description", message.getDescription()).append("pubDate", date));
			return true;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return false;
	}
	@Override
	public ArrayList<Document> getNews() {
		MongoClient mongoClient = getInstance();
		try {
			FindIterable<Document> iterable = getCollection(mongoClient).find();
			ArrayList<Document> newsAl = findIterableToArraylist(iterable);
			return newsAl;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return null;
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
		try {
			if (word == null || word.equals("")) {
				return false;
			}
			Date dateConverted = dateUtils.dateConvert(dateStr);
			try {
				UpdateResult result = getCollection(mongoClient).updateOne(
						new Document("date", dateConverted).append("word", word),
						new Document("$inc", new Document("frequency", frequency)));
				return result.wasAcknowledged();
			} catch (MongoWriteException e) {
				LOG.error("Data couldn't be updated.", e);
			}
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#fetch(Document
	 * find,Document sort,int limit)
	 */
	@Override
	public ArrayList<Document> fetch(Document find, Document sort, int limit) {
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

	@Override
	public boolean findNew(FeedMessage message) {
		if (message == null || message.getTitle() == null || message.getTitle().equals("")
				|| message.getDescription() == null || message.getDescription().equals("")
				|| message.getpubDate() == null || message.getpubDate().equals("")) {
			return false;
		}
		long count=0;
		Date date = dateUtils.dateConvert(dateUtils.dateCustomize(message.getpubDate()));
		try {
			Bson filter = new Document().append("pubDate", date).append("title", message.getTitle()).append("description", message.getDescription());
			MongoClient mongoClient = getInstance();
			count =  getCollection(mongoClient).count(filter);
			if (count > 0) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("News find process have a troble.",e);
		}
		return false;
	}
}