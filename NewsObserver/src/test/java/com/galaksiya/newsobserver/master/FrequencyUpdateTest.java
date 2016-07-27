package com.galaksiya.newsobserver.master;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
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
	public void addDatabaseInvalidEmptyList(){
		List<Document> docList = new ArrayList<>();
		assertFalse(dbHelper.addDatabase(docList));
	}
	@Test
	public void addDatabaseInvalidNullList(){
		assertFalse(dbHelper.addDatabase(null));
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
		String dateStr = "17 May 2016";
		String wordStr = "test";
		String customId = dateUtils.dateStrToHashForm(dateStr) +"_" + wordStr;
		Document document = new Document("_id",customId).append("date",dateUtils.dateConvert(dateStr)).append("word", wordStr).append("frequency", 2);
		List<Document> docList = new ArrayList<>();
		docList.add(document);
		dbHelper.addDatabase(docList);
		ArrayList<String> dateWordFreq = (ArrayList<String>) mongo.fetchFirstWDocument();
		String date = dateWordFreq.get(0);
		String word = dateWordFreq.get(1);
		int frequency = Integer.parseInt(dateWordFreq.get(2));
		date = dateUtils.dateCustomize(date);
		dbHelper.addDatabase(docList);
		dateWordFreq = (ArrayList<String>) mongo.fetchFirstWDocument();
		int frequencyNew = Integer.parseInt(dateWordFreq.get(2));
		assertEquals(frequencyNew, frequency + 2);
	}
}
