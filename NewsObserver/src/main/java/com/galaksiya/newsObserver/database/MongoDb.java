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
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoDb implements Database {

	private static final Logger LOG = Logger.getLogger(MongoDb.class);

	private String collectionName;

	public MongoDb() {
	}

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

	public MongoDb(String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * insert
	 * <p>
	 * process
	 * <p>
	 * date-word-frequency
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#save(java.lang.String,
	 *      java.lang.String, int)
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

	@Override
	public boolean update(String dateStr, String word, int frequency) { // update
																		// and
																		// increment
																		// the
																		// frequency
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

	public MongoClient newClient() {
		return new MongoClient("localhost", 27017);
	}

	@Override
	public FindIterable<Document> fetch(String date) { // frequency sorted from
														// a date for all
		// words
		try (MongoClient mongoClient = newClient()) {
			Date dateConverted = dateConvert(date);
			FindIterable<Document> iterable = getCollection(mongoClient)
					.find(new Document().append("date", dateConverted)).sort(new Document().append("frequency", -1));
			printerOfFindIterable(iterable);
			return iterable;
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return null;
	}

	public FindIterable<Document> fetch(String date, int limit) { // frquency
																	// sorted
																	// for top
																	// 10
		// word
		try (MongoClient mongoClient = newClient()) {
			Date dateConverted = dateConvert(date);
			FindIterable<Document> iterable = getCollection(mongoClient)
					.find(new Document().append("date", dateConverted)).sort(new Document().append("frequency", -1))
					.limit(limit);
			printerOfFindIterable(iterable);
			return iterable;
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return null;
	}

	public FindIterable<Document> fetch() { // frequency sorted for all the
											// world
		try (MongoClient mongoClient = newClient()) {

			FindIterable<Document> iterable = getCollection(mongoClient).find(new Document())
					.sort(new Document().append("frequency", -1));
			printerOfFindIterable(iterable);
			return iterable;
		} catch (Exception e) {
			LOG.error("Mongo Connection Exception", e);
		}
		return null;
	}

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

	private void printerOfFindIterable(FindIterable<Document> iterable) { // which
																			// provide
																			// to
																			// print
																			// document
																			// in
																			// collection
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

	public long contain(String dateStr, String word) { // return -1:problem
		if (word == null || word.equals("")) {
			return -1;
		}
		Date date = dateConvert(dateStr);

		Bson filter = new Document().append("date", date).append("word", word);

		try (MongoClient mongoClient = newClient();) {
			return getCollection(mongoClient).count(filter);
		} // sayısını

	}

	public long totalCount() {
		try (MongoClient mongoClient = newClient()) {
			return getCollection(mongoClient).count(new Document());
		}
	}

	private Date dateConvert(String dateStr) {
		Boolean flag = false;
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yy");
		Date date = null;
		do {
			dateStr = askAgain(dateStr, flag);
			try {
				date = format1.parse(dateStr.replaceAll("\\s+", "-"));
				flag = false;
				LOG.debug("Input(String) converted date successfully.");

			} catch (ParseException e) {
				flag = true;
				LOG.error("Input(String) couldn't convert to date.It will be requested again. ", e);
			} // date is the our object's date
		} while (flag);
		return date;
	}

	public String askAgain(String dateStr, Boolean flag) {
		if (flag) {
			System.out.println("BE CAREFUL.Insert a date of day like :  17 Mar 2016\n");
			try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
				dateStr = input.readLine();
			} catch (IOException e) {
				LOG.error("Input(String) couldn't convert to date. ", e);
			}
		}
		return dateStr;
	}
}
