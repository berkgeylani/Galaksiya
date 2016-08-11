package com.galaksiya.newsobserver.database;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.galaksiya.newsobserver.parser.FeedMessage;
/**
 * This interface contains database functions.
 * @author francium
 *
 */
public interface Database {

	/**
	 * It search in selected date and word and return count of it.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @return -1: Fault Others: Success
	 */
	long contain(Date date, String word);

	/**
	 * Delete all the data from database
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean delete();
	
	
	/**
	 * Prints all the words sorted in frequency
	 * @return a size(int) which will be printed
	 */
	List<Document> fetch();

	/**
	 * Prints all the words sorted in frequency in a selected date
	 * @param date A date(String) which will be selected. 
	 * @return a size(int) which will be printed
	 */
	List<Document> fetch(String date);

	/**
	 * Prints all the words sorted in frequency in a selected date with a limit
	 * @param date A date(String) which will be selected. 
	 * @param limit : It limits size of data which will be printed
	 * @return a size(int) which will be printed
	 */
	List<Document> fetch(String date, int limit);

	/**
	 * It provides us to get sample from database(first).
	 * @return null : fault | arraylist indexes: 0-date   1-word  2-frequency
	 */
	List<String> fetchFirstWDocument();

	/**
	 * It insert to Db which is selected date-word-frequency
	 * @param dateStr Date of a new.
	 * @param word	A word which used in new.
	 * @param frequency Frequency of a word in new.
	 * @see com.galaksiya.newsobserver.database.Database#save(java.lang.String,
	 *      java.lang.String, int)
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean save(String dateStr, String word, int frequency);

	/**
	 * It insert a list to database
	 * @param insertList A document list which is many (date-word frequency).
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean saveMany(List<Document> insertList);
	
	
	/**
	 * It insert news to Db one by one.
	 * @param message It occurs from publishDate-Title-Description
	 * @see com.galaksiya.newsobserver.database.Database#save(java.lang.String,
	 *      java.lang.String, int)
	 * @return true:if process has been successfully done. false: If it fault.
	 */
	boolean saveNews(FeedMessage message);
	/**
	 * It fetch all the news in the database.
	 * @return It returns with arrayList which contains title-description-pubdate.
	 */
	List<Document> getNews();
	/**
	 * It find the new which is given in message.
	 * @param message It occurs from publishDate-Title-Description
	 * @return true: It find false: There is no new in database like in message.
	 */
	boolean exists(FeedMessage message);
	
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
	List<Document> fetch(Document find,Document sort,int limit);

}