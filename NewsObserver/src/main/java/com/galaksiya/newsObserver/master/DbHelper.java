package com.galaksiya.newsObserver.master;



import com.galaksiya.newsObserver.database.MongoDb;

public class DbHelper {
	private MongoDb mongoDbHelper;
	
	private static class _DbHelperHold{
		private final static DbHelper INSTANCE = new DbHelper();
	}
	/**
	 * It return a singleton MongoDb object
	 */
	public static DbHelper getInstance(){
		return _DbHelperHold.INSTANCE;
	}
	
	
	private NewsChecker newsCheckerTest = new NewsChecker();
	
	/**
	 * It provide us to create default MongoDb collection name which is statistics.
	 */
	public DbHelper(){
		mongoDbHelper = new MongoDb();
	}
	
	/**
	 * It provide us to select collection name.
	 * @param collection String to set collection name.
	 */
	public DbHelper(String collection){
		mongoDbHelper = new MongoDb(collection);
	}
	/**
	 * It takes date-word and frequency then if it is in database,it increments.If not it inserts it.
	 * @param datePerNew Date which will be search.
	 * @param word	Word which will be search.
	 * @param frequency Frequency of a word.
	 * @return flagSuccessful Successful Flag(True: Okay False : Fault)
	 */
	public boolean addDatabase(String datePerNew,String word,int frequency){
		boolean flagSuccessful;
		if(!newsCheckerTest.canConvert(datePerNew)) return false;
		boolean alreadyInsertedToDatabase=mongoDbHelper.contain(datePerNew, word) >= 1;
		if(alreadyInsertedToDatabase){  
			flagSuccessful=mongoDbHelper.update(datePerNew, word, frequency);
		}
		else {
			flagSuccessful=mongoDbHelper.save(datePerNew, word, frequency);
		}
		return flagSuccessful;
	}
}
