package com.galaksiya.newsObserver.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoDb implements Database {

	private static final Logger LOG = Logger.getLogger(MongoDb.class);

	private String collectionName;

	public MongoDb() {
	}
	/**
	 * It returns MongoCollection with selected collection name(Default: statistics).It set when initialize MongoDb.
	 * @param client Get a MongoClient which is set "localhost", 27017.
	 * @return collection : Default: initialize statistics collection. You can set when create MongoDb(//here).
	 */
	public MongoCollection<Document> getCollection(MongoClient client) {
		MongoDatabase mDatabase = client.getDatabase("mydb");
		MongoCollection<Document> collection = null;
		if (collectionName == null) {
			collection = mDatabase.getCollection("statistics");
		} else {
			collection = mDatabase.getCollection(collectionName);
		}
		return collection;
	}
	
	/**
	 * It provide us to select collection name.
	 * @param collectionName String to set collection name.
	 */
	public MongoDb(String collectionName) {
		this.collectionName = collectionName;
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
		try (MongoClient mongoClient = newClient()) {
			if (word == null || word.equals("")) {
				return false;
			}
			try {
				Date date = dateConvert(dateStr);
				getCollection(mongoClient).insertOne(
						new Document().append("date", date).append("word", word).append("frequency", frequency));
				return true;

			} catch (MongoWriteException e) {
				LOG.error("Data couldn't be inserted.", e);

			}
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return false;
	}
	
	/**
	 * Delete all the data from database
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	@Override
	public boolean delete() {// Delete All documents from collection :Using
		// blank BasicDBObject
		try (MongoClient mongoClient = newClient()) {
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
	 * It increment frequency of a selected word,date.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @param frequency Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	@Override
	public boolean update(String dateStr, String word, int frequency) {
		try (MongoClient mongoClient = newClient()) {
			if (word == null || word.equals("")) {
				return false;
			}
			Date dateConverted = dateConvert(dateStr);
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
	 * Fabric of a MongoClient
	 * @return a MongoClient which parameters are "localhost",27017 (dbAdress,port).
	 */
	
	public MongoClient newClient() {
		return new MongoClient("localhost", 27017);
	}
	/*public MongoClient newClient() {
//		MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
//		builder.connectionsPerHost(100);
		MongoClientURI mongoClientURI = new MongoClientURI("mongodb://accountUser:password@localhost:27017/mydb?maxPoolSize=100");
		return new MongoClient(mongoClientURI);
	}*/
	/**
	 * Prints all the words sorted in frequency
	 * @return a size(int) which will be printed
	 */
	public int fetch() { // frequency sorted for all the words
		return fetchMain(new Document(), new Document().append("frequency", 1), -1);
	}
	/**
	 * Prints all the words sorted in frequency in a selected date
	 * @param date A date(String) which will be selected. 
	 * @return a size(int) which will be printed
	 */
	public int fetch(String date) { 
		Date dateConverted = dateConvert(date);
		return fetchMain(new Document().append("date", dateConverted), new Document().append("frequency", 1), -1);

	}
	/**
	 * Belirli bir günün limitli olarak verileri çeker
	 * @param date A date(String) which will be selected. 
	 * @param limit : It limits size of data which will be printed
	 * @return a size(int) which will be printed
	 */
	public int fetch(String date, int limit) { 
		Date dateConverted = dateConvert(date);
		return fetchMain(new Document().append("date", dateConverted), new Document().append("frequency", -1), limit);
	}
	
	/**
	 * All fetch function uses this and this is query creator.
	 * @param find  : document creator of find query
	 * @param sort  : document creator of sort query
	 * @param limit : It limits size of data which will be printed 
	 * @return -1 : fault | result is bigger than "0" it is success
	 */
	private int fetchMain(Document find,Document sort,int limit){
		// world
		try (MongoClient mongoClient = newClient()) {
			FindIterable<Document> iterable = getCollection(mongoClient).find(find)
					.sort(sort);
			if(limit>=0)
				iterable=iterable.limit(limit);
			printerOfFindIterable(iterable);
			return iteratorSize(iterable);
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return -1;
	}
	/**
	 * It provides us to get sample from database(first).
	 * @return null : fault | arraylist indexes: 0-date   1-word  2-frequency
	 */
	public ArrayList<String> fetchFirstWDocument() {
		try (MongoClient mongoClient = newClient()) {
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
	 * It gives count of documents in FindIterable which is param
	 * @param iterable It is a Mongo query response object.
	 * @return A int which is count of documents.
	 */
	public int iteratorSize(FindIterable<Document> iterable){
		try (MongoClient mongoClient = newClient()) {
			int size = 0;
			MongoCursor<Document> cursor = iterable.iterator();
			while (cursor.hasNext()) {
				size++;
				cursor.next();
			}
			return size;
		}
		catch (Exception e) {
			return -1;
		}
	}
	/**
	 * It prints documents(date-word-freq) in FindIterable(param).
	 * @param iterable It is a Mongo query response object.
	 */
	private void printerOfFindIterable(FindIterable<Document> iterable) {
		// TODO if nothing to show say in here There is nothing to show
		System.out.println("----Date----" + "\t\t\t\t" + "----Word----" + "\t" + "----Frequency----");

		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				System.out.println(
						document.get("date") + "\t\t" + document.get("word") + "\t\t" + document.get("frequency"));
			}
		});
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
		Date date = dateConvert(dateStr);

		Bson filter = new Document().append("date", date).append("word", word);

		try (MongoClient mongoClient = newClient();) {
			return getCollection(mongoClient).count(filter);
		} // sayısını

	}
	/**
	 * It gives total count of a data in database.
	 * @return Count of a data.
	 */
	public long totalCount() {
		try (MongoClient mongoClient = newClient()) {
			return getCollection(mongoClient).count(new Document());
		}
	}
	/**
	 * It converts String to Date format to search in query.
	 * @param dateStr Given date in String format.
	 * @return Return Date format(dd-MMM-yy).
	 */
	private Date dateConvert(String dateStr) {
		Boolean flag = false;
		int i=0;
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yy");
		Date date = null;
		do {
			if(i>=1) 
				dateStr = askAgain(dateStr);
			try {
				date = format1.parse(dateStr.replaceAll("\\s+", "-"));
				flag = false;
				LOG.debug("Input(String) converted date successfully.");

			} catch (ParseException e) {
				flag = true;
				i++;
				LOG.error("Input(String) couldn't convert to date.It will be requested again. ", e);
			} // date is the our object's date
		} while (flag);
		return date;
	}
	/**
	 * It asks date again for wrong input.
	 * @param dateStr Given date in String format.
	 * @return It returns date in String format.
	 */
	public String askAgain(String dateStr) {
			System.out.println("BE CAREFUL.Insert a date of day like :  17 Mar 2016\n");
			try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
				dateStr = input.readLine();
			} catch (IOException e) {
				LOG.error("Input(String) couldn't convert to date. ", e);
			}
		return dateStr;
	}
}
