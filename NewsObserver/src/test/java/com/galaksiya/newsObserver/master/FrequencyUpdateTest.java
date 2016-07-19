package com.galaksiya.newsObserver.master;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsobserver.database.Database;
import com.galaksiya.newsobserver.database.MongoDb;
import com.galaksiya.newsobserver.master.DateUtils;
import com.galaksiya.newsobserver.master.FrequencyUpdater;

public class FrequencyUpdateTest {

	private static final String COLLECTION_NAME = "test";

	private FrequencyUpdater dbHelper;

	private Database mongo;

	@Test
	public void addDatabaseInvalidDate(){
		assertFalse(dbHelper.addDatabase("qwerqwer", "test", 2));
	}

	@After
	public void after() {
		mongo.delete();
	}

	@Before
	public void before() {
		mongo = new MongoDb(COLLECTION_NAME);
		dbHelper = new FrequencyUpdater(mongo);
	}
	
	@Test
	public void updateDatabaseControl() {
		DateUtils dateUtils = new DateUtils();
		dbHelper.addDatabase("17 May 2016", "test", 2);
		ArrayList<String> dateWordFreq = (ArrayList<String>) mongo.fetchFirstWDocument();
		String date = dateWordFreq.get(0);
		String word = dateWordFreq.get(1);
		int frequency = Integer.parseInt(dateWordFreq.get(2));
		date = dateUtils.dateCustomize(date);
		dbHelper.addDatabase(date, word, 2);
		dateWordFreq = (ArrayList<String>) mongo.fetchFirstWDocument();
		int frequencyNew = Integer.parseInt(dateWordFreq.get(2));
		assertEquals(frequencyNew, frequency + 2);
	}
}
