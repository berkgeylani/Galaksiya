package com.galaksiya.newsObserver.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsObserver.master.DateUtils;
import com.galaksiya.newsObserver.parser.FeedMessage;

public class MongoDbTest {
	private Database mongoDb;
	private String date;
	private String word;

	@Before
	public void before() {
		mongoDb = new MongoDb("test");
		word = "test";
		date = "17-May-2016";
	}

	@After
	public void After() {
		mongoDb.delete();
	}

	@Test
	public void containNullInput() {
		assertEquals(-1, mongoDb.contain(date, null));
	}

	@Test
	public void containInvalidInput() {
		assertEquals(-1, mongoDb.contain(date, ""));
	}

	@Test
	public void saveNullInput() {
		assertFalse(mongoDb.save(date, null, 2));
	}

	@Test
	public void saveInvalidInput() {
		assertFalse(mongoDb.save(date, "", 2));
	}

	@Test
	public void updateNullInput() {
		assertFalse(mongoDb.update(date, null, 2));
	}

	@Test
	public void updateInvalidInput() {
		assertFalse(mongoDb.update(date, "", 2));
	}

	@Test
	public void save() {
		assertEquals(0, mongoDb.contain(date, word));
		assertTrue(mongoDb.save(date, word, 2));
		assertEquals(1, mongoDb.contain(date, word));
	}

	@Test
	public void delete() {
		mongoDb.save(date, word, 3);
		mongoDb.save("17 Mar 2015", "test2", 4);
		assertEquals(2, mongoDb.totalCount());
		assertTrue(mongoDb.delete());
		assertEquals(0, mongoDb.totalCount());
	}

	@Test
	public void canUpdate() {
		mongoDb.save(date, word, 2);
		assertTrue(mongoDb.update(date, word, 2));
	}

	@Test
	public void updateCanIncrement() {
		mongoDb.save(date, word, 2);
		int frequencyLocal = Integer.parseInt(mongoDb.fetchFirstWDocument().get(2));
		mongoDb.update(date, word, 2);
		assertEquals(Integer.parseInt(mongoDb.fetchFirstWDocument().get(2)), frequencyLocal + 2);
	}


	@Test
	public void overrideFetch() {
		for (int i = 0; i < 12; i++) {
			mongoDb.save(date, word, 2);
		}
		assertEquals(mongoDb.totalCount(), (mongoDb.fetch().size()));
	}

	@Test
	public void overrideFetchDate() {
		mongoDb.save(date, word, 2);
		mongoDb.save(date, word, 2);
		mongoDb.save(date, word, 2);
		mongoDb.save(date, word, 2);
		assertEquals(mongoDb.contain(date, word), mongoDb.fetch(date).size());
	}

	@Test
	public void overrideFetchDateLimit() {
		mongoDb.save(date, word, 2);
		mongoDb.save(date, word, 2);
		mongoDb.save(date, word, 2);
		mongoDb.save(date, word, 2);
		assertEquals(2, mongoDb.fetch(date, 2).size());
	}

	@Test
	public void fetchFirstWDOcument() {
		ArrayList<String> firstDoc = mongoDb.fetchFirstWDocument();
		if (firstDoc != null)
			fail("fetchFirstDocument test has broken because of :" + firstDoc.get(0) + "\t" + firstDoc.get(1) + "\t"
					+ firstDoc.get(2) + "\t in added database");
		mongoDb.save(date, word, 2);
		firstDoc = mongoDb.fetchFirstWDocument();
		assertFalse(firstDoc.isEmpty());
	}

	@Test
	public void contain() {
		assertEquals(0, mongoDb.contain(date, word));
		mongoDb.save(date, word, 2);
		assertEquals(1, mongoDb.contain(date, word));
	}

	@Test
	public void totalCount() {
		assertEquals(0, mongoDb.totalCount());
		for (int i = 0; i < 6; i++) {
			mongoDb.save(date, word, 2);
		}
		assertEquals(6, mongoDb.totalCount());
	}
	@Test
	public void saveNewsNullTitle(){
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle(null);
		message.setDescription("Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}
	@Test
	public void saveNewsNullDescription(){
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(null);
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}
	@Test
	public void saveNewsNullPubdate(){
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription("Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate(null);
		assertFalse(mongoDb.saveNews(message));
	}
	@Test
	public void saveNewsEmptyTitle(){
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription("Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}
	@Test
	public void saveNewsEmptyDescription(){
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");		
		assertFalse(mongoDb.saveNews(message));
	}
	@Test
	public void saveNewsEmptyPubDate(){
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("");
		assertFalse(mongoDb.saveNews(message));
	}
	@Test
	public void saveNewscanInsert(){
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		mongoDb.saveNews(message);
		assertEquals( 1 , mongoDb.totalCount());
	}
	@Test
	public void saveNewInsertDataControl(){  ////////////////////////////
		DateUtils dateUtils = new DateUtils();
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		mongoDb.saveNews(message);
		ArrayList<Document> newsAL = mongoDb.getNews();
		if(newsAL == null)
			fail("Couldn't insert.");
		Document document = newsAL.get(0);
		boolean isTitleDescPubDateEqualsWithDatabase = document.get("title").equals(message.getTitle()) && document.get("description").equals(message.getDescription());
		assertEquals(dateUtils.dateCustomize(document.get("pubDate").toString()),dateUtils.dateCustomize("Mon May 02 00:00:00 EEST 2016"));
		assertTrue(isTitleDescPubDateEqualsWithDatabase);
	}
}
