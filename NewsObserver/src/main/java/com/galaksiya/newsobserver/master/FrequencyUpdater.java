package com.galaksiya.newsobserver.master;

import com.galaksiya.newsobserver.database.Database;

public class FrequencyUpdater {

	private Database database;

	public FrequencyUpdater(Database dbObject) {
		this.database = dbObject;
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
		boolean alreadyInsertedToDatabase = database.contain(datePerNew, word) >= 1;
		if (alreadyInsertedToDatabase) {
			flagSuccessful = database.update(datePerNew, word, frequency);
		} else {
			flagSuccessful = database.save(datePerNew, word, frequency);
		}
		return flagSuccessful;
	}

}
