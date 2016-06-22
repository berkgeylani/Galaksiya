package com.galaksiya.newsObserver.database;


import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.galaksiya.newsObserver.master.DateUtils;
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

	private static MongoClient mongoClient;

	/**
	 * Fabric of a MongoClient
	 * @return a MongoClient which parameters are "localhost",27017 (dbAdress,port).
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

	private DateUtils data = new DateUtils();

	public MongoDb() {
	}

	/**
	 * It provide us to select collection name.
	 * @param collectionName String to set collection name.
	 */
	public MongoDb(String collectionName) {
		this.data.collectionName = collectionName;
	}

	

	/**
	 * It search in selected date and word and return count of it.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @return -1: Fault Others: Success
	 */
	public long contain(String dateStr, String word) { 
		if (word == null || word.equals("")) {
			return -1;
		}
		Date date = data.dateConvert(dateStr);

		Bson filter = new Document().append("date", date).append("word", word);

		MongoClient mongoClient = getInstance();
		return getCollection(mongoClient).count(filter);
	}
	/**
	 * Delete all the data from database
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	@Override
	public boolean delete() {// Delete All documents from collection :Using
		// blank BasicDBObject
		MongoClient mongoClient = getInstance();
		try{
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
	/**
	 * Prints all the words sorted in frequency
	 * @return a size(int) which will be printed
	 */
	public ArrayList<Document> fetch() { // frequency sorted for all the words
		return fetch(new Document(), new Document().append("frequency", 1), -1);
	}
	/**
	 * Prints all the words sorted in frequency in a selected date
	 * @param date A date(String) which will be selected. 
	 * @return a size(int) which will be printed
	 */
	public ArrayList<Document> fetch(String date) { 
		Date dateConverted = data.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", 1), -1);

	}
	/**
	 * Belirli bir günün limitli olarak verileri çeker
	 * @param date A date(String) which will be selected. 
	 * @param limit : It limits size of data which will be printed
	 * @return a size(int) which will be printed
	 */
	public ArrayList<Document> fetch(String date, int limit) { 
		Date dateConverted = data.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", -1), limit);
	}
	/**
	 * It provides us to get sample from database(first).
	 * @return null : fault | arraylist indexes: 0-date   1-word  2-frequency
	 */
	public ArrayList<String> fetchFirstWDocument() {
		MongoClient mongoClient = getInstance();
		try{
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
	 * It returns MongoCollection with selected collection name(Default: statistics).It set when initialize MongoDb.
	 * @param client Get a MongoClient which is set "localhost", 27017.
	 * @return collection : Default: initialize statistics collection. You can set when create MongoDb(//here).
	 */
	public MongoCollection<Document> getCollection(MongoClient client) {
		MongoDatabase mDatabase = client.getDatabase("mydb");
		MongoCollection<Document> collection = null;
		if (data.collectionName == null) {
			collection = mDatabase.getCollection("statistics");
		} else {
			collection = mDatabase.getCollection(data.collectionName);
		}
		return collection;
	}
	/**
	 * It gives count of documents in FindIterable which is param
	 * @param iterable It is a Mongo query response object.
	 * @return A int which is count of documents.
	 */
	public ArrayList<Document> iteratorSize(FindIterable<Document> iterable){
		ArrayList<Document> dataAL = new ArrayList<Document>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Document document = cursor.next();
			dataAL.add(document);
		}
		return dataAL;

	}
	/**
	 * It insert to mongoDb which is selected date-word-frequency
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#save(java.lang.String,
	 *      java.lang.String, int)
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	@Override
	public boolean save(String dateStr, String word, int frequency) {
		MongoClient mongoClient = getInstance();
		if (word == null || word.equals("")) {
			return false;
		}
		try {
			Date date = data.dateConvert(dateStr);
			getCollection(mongoClient).insertOne(
					new Document().append("date", date).append("word", word).append("frequency", frequency));
			return true;

		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);

		}
		return false;
	}
	/**
	 * It gives total count of a data in database.
	 * @return Count of a data.
	 */
	public long totalCount() {
		return getCollection(getInstance()).count(new Document());
	}
	/**
	 * It increment frequency of a selected word,date.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @param frequency Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	@Override
	public boolean update(String dateStr, String word, int frequency) {
		MongoClient mongoClient = getInstance();
		try{
			if (word == null || word.equals("")) {
				return false;
			}
			Date dateConverted = data.dateConvert(dateStr);
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
	
	/**
	 * All fetch function uses this and this is query creator.
	 * @param find  : document creator of find query
	 * @param sort  : document creator of sort query
	 * @param limit : It limits size of data which will be printed 
	 * @return -1 : fault | result is bigger than "0" it is success
	 */
	private ArrayList<Document> fetch(Document find,Document sort,int limit){
		MongoClient mongoClient = getInstance();
		try{
			FindIterable<Document> iterable = getCollection(mongoClient).find(find)
					.sort(sort);
			if(limit>=0)
				iterable=iterable.limit(limit);
			//printerOfFindIterable(iterable);
			return iteratorSize(iterable);
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return null;
	}
}