package com.galaksiya.newsObserver.database;

import java.util.ArrayList;

import org.bson.Document;

import com.galaksiya.newsObserver.parser.FeedMessage;

public interface Database {

	/**
	 * It search in selected date and word and return count of it.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @return -1: Fault Others: Success
	 */
	long contain(String dateStr, String word);

	/**
	 * Delete all the data from database
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean delete();

	/**
	 * Prints all the words sorted in frequency
	 * @return a size(int) which will be printed
	 */
	ArrayList<Document> fetch();

	/**
	 * Prints all the words sorted in frequency in a selected date
	 * @param date A date(String) which will be selected. 
	 * @return a size(int) which will be printed
	 */
	ArrayList<Document> fetch(String date);

	/**
	 * Prints all the words sorted in frequency in a selected date with a limit
	 * @param date A date(String) which will be selected. 
	 * @param limit : It limits size of data which will be printed
	 * @return a size(int) which will be printed
	 */
	ArrayList<Document> fetch(String date, int limit);

	/**
	 * It provides us to get sample from database(first).
	 * @return null : fault | arraylist indexes: 0-date   1-word  2-frequency
	 */
	ArrayList<String> fetchFirstWDocument();

	/**
	 * It insert to mongoDb which is selected date-word-frequency
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#save(java.lang.String,
	 *      java.lang.String, int)
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean save(String dateStr, String word, int frequency);

	/**
	 * It insert news to mongoDb one by one.
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#save(java.lang.String,
	 *      java.lang.String, int)
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean saveNews(FeedMessage message);
	/**
	 * It fetch all the news in the database.
	 * @return It returns with arrayList which contains title-description-pubdate.
	 */
	public ArrayList<Document> getNews();
	
	/**
	 * It gives total count of a data in database.
	 * @return Count of a data.
	 */
	long totalCount();

	/**
	 * It increment frequency of a selected word,date.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @param frequency Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	boolean update(String dateStr, String word, int frequency);
	/**
	 * All fetch function uses this and this is query creator.
	 * @param find  : document creator of find query
	 * @param sort  : document creator of sort query
	 * @param limit : It limits size of data which will be printed 
	 * @return -1 : fault | result is bigger than "0" it is success
	 */
	ArrayList<Document> fetch(Document find,Document sort,int limit);

}