package com.galaksiya.newsObserver.master;

import com.galaksiya.newsObserver.database.Database;
import com.galaksiya.newsObserver.database.DatabaseFactory;

public class DbHelper {

	private Database dbHelper;

	private DatabaseFactory databaseFactory = DatabaseFactory.getInstance();

	/**
	 * It provide us to create default MongoDb collection name which is
	 * statistics.
	 */
	public DbHelper() {
		dbHelper = databaseFactory.getDatabase();
	}

	/**
	 * It provide us to select collection name.
	 * 
	 * @param collection
	 *            String to set collection name.
	 */
	public DbHelper(String collection) {
		dbHelper = databaseFactory.getDatabase(collection);
	}

	public DbHelper(Database dbObject) {
		dbHelper = dbObject;
	}

	/**
	 * It takes date-word and frequency then if it is in database,it
	 * increments.If not it inserts it.
	 * 
	 * @param datePerNew
	 *            Date which will be search.
	 * @param word
	 *            Word which will be search.
	 * @param frequency
	 *            Frequency of a word.
	 * @return flagSuccessful Successful Flag(True: Okay False : Fault)
	 */
	public boolean addDatabase(String datePerNew, String word, int frequency) {
		boolean flagSuccessful;
		DateUtils dateUtils = new DateUtils();
		if (!dateUtils.canConvert(datePerNew))
			return false;
		boolean alreadyInsertedToDatabase = dbHelper.contain(datePerNew, word) >= 1;
		if (alreadyInsertedToDatabase) {
			flagSuccessful = dbHelper.update(datePerNew, word, frequency);
		} else {
			flagSuccessful = dbHelper.save(datePerNew, word, frequency);
		}
		return flagSuccessful;
	}

}
