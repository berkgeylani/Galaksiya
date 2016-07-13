package com.galaksiya.newsObserver.master;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsObserver.database.Database;
import com.galaksiya.newsObserver.database.MongoDb;

public class DbHelperTest {

	private static final String COLLECTION_NAME = "test";
	
	private DbHelper dbHelper;
	
	private Database mongo ;
	
	@Before 
	public void before(){
		mongo= new  MongoDb(COLLECTION_NAME);
		dbHelper = new DbHelper(mongo);
	}
	
	@After
	public void after(){
		mongo.delete();
	}

	@Test
	public void updateDatabaseControl() {
		DateUtils dateUtils = new DateUtils();
		dbHelper.addDatabase("17 May 2016", "test", 2);
		ArrayList<String> dateWordFreq = mongo.fetchFirstWDocument();
		String date = dateWordFreq.get(0);
		String word = dateWordFreq.get(1);
		int frequency = Integer.parseInt(dateWordFreq.get(2));
		date = dateUtils.dateCustomize(date);
		dbHelper.addDatabase(date, word, 2);
		dateWordFreq = mongo.fetchFirstWDocument();
		int frequencyNew = Integer.parseInt(dateWordFreq.get(2));
		assertEquals(frequencyNew , frequency + 2 );
	}
}
