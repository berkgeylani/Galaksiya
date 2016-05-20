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
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoDb implements Database {
	private MongoClient mongoClient;
	private MongoDatabase mDatabase;
	private MongoCollection<Document> collection;
	private static final Logger LOG = Logger.getLogger(MongoDb.class);

	// infos for mongodb
	public MongoDb() {
		try {
			mongoClient = new MongoClient("localhost", 27017);
			mDatabase = mongoClient.getDatabase("mydb");
			collection = mDatabase.getCollection("statistics");
		} catch (MongoException e) {
			LOG.error("Mongo Connection Error", e);
		}
	}
	public MongoDb(String collections) {
		try {
			mongoClient = new MongoClient("localhost", 27017);
			mDatabase = mongoClient.getDatabase("mydb");
			collection = mDatabase.getCollection(collections);
		} catch (MongoException e) {
			LOG.error("Mongo Connection Error", e);
		}
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	@Override
	public boolean save(String dateStr, String word, int frequency) { // insert process date-word-frequency
		if(word==null || word.equals("")){
			return false;
		}
		try {
			Date date = dateConvert(dateStr);
			collection
					.insertOne(new Document().append("date", date).append("word", word).append("frequency", frequency));
			return true;

		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);

		}
		return false;
	}

	@Override
	public boolean delete() {// Delete All documents from collection :Using blank BasicDBObject
		try {
			collection.deleteMany(new Document());
			LOG.info("All data deleted");
			return true;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be deleted.", e);
			return false;
		}

	}

	@Override
	public boolean update(String dateStr, String word, int frequency) { // update  and increment the frequency
		if(word==null || word.equals("")){
			return false;
		}
		Date dateConverted = dateConvert(dateStr);
		try {
			UpdateResult result=collection.updateOne(new Document("date", dateConverted).append("word", word),
					new Document("$inc", new Document("frequency", frequency)));
			return result.wasAcknowledged();
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be updated.", e);
			return false;
		}
	}

	@Override
	public FindIterable<Document> fetch(String date) { // frequency sorted from a date for all
										// words
		
		Date dateConverted = dateConvert(date);

		FindIterable<Document> iterable = collection.find(new Document().append("date", dateConverted))
				.sort(new Document().append("frequency", -1));
		printerOfFindIterable(iterable);
		return iterable;

	}

	public FindIterable<Document> fetch(String date, int limit) { // frquency sorted for top 10
												// word
		Date dateConverted = dateConvert(date);

		FindIterable<Document> iterable = collection.find(new Document().append("date", dateConverted))
				.sort(new Document().append("frequency", -1)).limit(limit);

		printerOfFindIterable(iterable);
		return iterable;
	}

	public FindIterable<Document> fetch() { // frequency sorted for all the world

		FindIterable<Document> iterable = collection.find(new Document())
				.sort(new Document().append("frequency", -1));
		printerOfFindIterable(iterable);
		return iterable;
	}
	public ArrayList<String> fetchFirstWDocument() {
		ArrayList<String> date_word_freq = new ArrayList<String>();
		FindIterable<Document> search = collection.find();
		if (search.first() == null) {
			return null;
		}
		date_word_freq.add(search.first().get("date").toString());
		date_word_freq.add(search.first().get("word").toString());
		date_word_freq.add(search.first().get("frequency").toString());
		return date_word_freq;
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
		if(word==null || word.equals("")){
			return -1;
		}
		Date date = dateConvert(dateStr);

		Bson filter = new Document().append("date", date).append("word", word);

		return collection.count(filter);// sayısını

	}
	public long totalCount(){
		return collection.count(new Document());
	}
	private Date dateConvert(String dateStr) {
		Boolean flag = false;
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yy");
		Date date = null;
		do {
			if (flag) {
				System.out.println("BE CAREFUL.Insert a date of day like :  17 Mar 2016\n");
				try {
					dateStr = input.readLine();
				} catch (IOException e) {
					LOG.error("Input(String) couldn't convert to date. ",e);
				}
			}
			try {
				date = format1.parse(dateStr.replaceAll("\\s+", "-"));
				flag = false;
				LOG.debug("Input(String) converted date successfully.");

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				flag = true;
				LOG.error("Input(String) couldn't convert to date.It will be requested again. ",e);
			} // date is the our object's date
		} while (flag);
		return date;
	}
}
