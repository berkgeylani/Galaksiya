package com.galaksiya.newsObserver.master;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsObserver.database.MongoDb;

public class DbHelperTest {

	private static final String COLLECTION_NAME = "test";
	
	private DbHelper dbHelper;
	
	private MongoDb mongo ;
	
	@Before 
	public void before(){
		mongo= new  MongoDb(COLLECTION_NAME);
		dbHelper = new DbHelper(COLLECTION_NAME);
	}
	
	@After
	public void after(){
		mongo.delete();
	}

	@Test
	public void updateDatabaseControl() {
		NewsChecker newsChecker = new NewsChecker();
		dbHelper.addDatabase("17 May 2016", "test", 2);
		ArrayList<String> dateWordFreq = mongo.fetchFirstWDocument();
		String date = dateWordFreq.get(0);
		String word = dateWordFreq.get(1);
		int frequency = Integer.parseInt(dateWordFreq.get(2));
		date = newsChecker.dateCustomize(date);
		dbHelper.addDatabase(date, word, 2);
		dateWordFreq = mongo.fetchFirstWDocument();
		int frequencyNew = Integer.parseInt(dateWordFreq.get(2));
		assertEquals(frequencyNew , frequency + 2 );
	}
}
