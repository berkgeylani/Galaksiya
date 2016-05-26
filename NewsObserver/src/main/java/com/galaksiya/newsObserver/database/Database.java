package com.galaksiya.newsObserver.database;

public interface Database {
	/**
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @param frequency Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	public boolean save(String dateStr,String word,int frequency);//which is insert opereation
	/**
	 * It deletes all the data from collection.
	 * @return true : Successful process. | false : It fault.
	 */
	public boolean delete();//which is remove opereation
	/**
	 * It increment frequency of a selected word,date.
	 * @param dateStr Date which will be search.
	 * @param word	Word which will be search.
	 * @param frequency Frequency of a word.
	 * @return true : Successful process. | false : It fault.
	 */
	public boolean update(String dateStr,String word,int frequency);//which is changing data opereation

}