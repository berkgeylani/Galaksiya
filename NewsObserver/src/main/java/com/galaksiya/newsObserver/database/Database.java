package com.galaksiya.newsObserver.database;

public interface Database {
	/**
	 * @param dateStr
	 *            Date which will be search.
	 * @param word
	 *            Word which will be search.
	 * @param frequency
	 *            Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	public boolean save(String dateStr, String word, int frequency);

	/**
	 * It deletes all the data from collection.
	 * 
	 * @return true : Successful process. | false : It fault.
	 */
	public boolean delete();

	/**
	 * It increment frequency of a selected word,date.
	 * 
	 * @param dateStr
	 *            Date which will be search.
	 * @param word
	 *            Word which will be search.
	 * @param frequency
	 *            Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	public boolean update(String dateStr, String word, int frequency);

}