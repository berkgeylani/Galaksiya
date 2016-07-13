package com.galaksiya.newsObserver.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsObserver.parser.FeedMessage;

public class DerbyDbTest {
	private DerbyDb derbyDb = new DerbyDb("TEST");

	@Before
	public void Before() throws SQLException {
		derbyDb.delete();
	}

	@Test
	public void containInvalidInput() {
		String date = "09-May-2016";
		assertEquals(-1, derbyDb.contain(date, ""));
	}

	@Test
	public void containNullInput() {
		String date = "09-May-2016";
		assertEquals(-1, derbyDb.contain(date, null));
	}

	@Test
	public void saveInvalidInput() {
		String date = "09-May-2016";
		assertFalse(derbyDb.save(date, "", 2));
	}

	@Test
	public void saveNewInsertDataControl() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		derbyDbForNews.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		derbyDbForNews.saveNews(message);
		ArrayList<Document> newsAL = derbyDbForNews.getNews();
		if (newsAL == null)
			fail("Couldn't insert.");
		Document document = newsAL.get(0);
		boolean isTitleDescPubDateEqualsWithDatabase = document.get("title").equals(message.getTitle())
				&& document.get("description").equals(message.getDescription());
		assertEquals(document.get("pubDate").toString(), "2016-05-02");
		assertTrue(isTitleDescPubDateEqualsWithDatabase);
	}

	@Test
	public void saveNewscanInsert() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		derbyDbForNews.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		derbyDbForNews.saveNews(message);
		assertEquals(1, derbyDbForNews.totalCount());
	}

	@Test
	public void saveNewsEmptyDescription() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsEmptyPubDate() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsEmptyTitle() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsNullDescription() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(null);
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsNullPubdate() {
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate(null);
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsNullTitle() {
		// given
		DerbyDb derbyDbForNews = new DerbyDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle(null);
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");

		// test
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNullInput() {
		String date = "09-May-2016";
		assertFalse(derbyDb.save(date, null, 2));
	}

	@Test
	public void testcanUpdate() {
		// Given
		String date = "09-May-2016";
		String word = "testWord";
		// when
		derbyDb.save(date, word, 2);
		// then
		assertTrue(derbyDb.update(date, word, 2));
	}

	@Test
	public void testContain() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";

		// When
		derbyDb.save(dateStr, word, 9);

		// then
		assertEquals(1, derbyDb.contain(dateStr, word));
	}

	@Test
	public void testDelete() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDb.save(dateStr, word, 9);
		derbyDb.save(dateStr, word, 11);

		// When
		derbyDb.delete();

		// then
		assertEquals(0, derbyDb.contain(dateStr, word));
	}

	@Test
	public void testFetch() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDb.save(dateStr, word, 9);
		derbyDb.save(dateStr, word, 11);

		// When
		ArrayList<Document> dateWordFreqAl = derbyDb.fetch();

		// then
		assertEquals(2, dateWordFreqAl.size());
		assertEquals(new Integer(11), dateWordFreqAl.get(1).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(1).getString("word").toString().equals("testWord"));
		assertTrue(dateWordFreqAl.get(1).getDate("date").toString().equals("2016-05-09"));
	}

	@Test
	public void testFetchFirstWDocument() {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDb.save(dateStr, word, 13);
		derbyDb.save(dateStr, word + "a", 9);
		derbyDb.save(dateStr, word + "b", 11);

		// When
		ArrayList<String> dateWordFreqAl = derbyDb.fetchFirstWDocument();

		// then
		assertEquals(3, dateWordFreqAl.size());
		assertEquals(13 + "", dateWordFreqAl.get(2));
		assertTrue(dateWordFreqAl.get(1).toString().equals("testWord"));
		assertTrue(dateWordFreqAl.get(0).toString().equals("2016-05-09"));
	}

	@Test
	public void testFetchString() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDb.save(dateStr, word, 9);
		derbyDb.save(dateStr, word, 11);
		derbyDb.save("03-May-2016", word, 11);

		// When
		ArrayList<Document> dateWordFreqAl = derbyDb.fetch("09-May-2016");

		// then
		assertEquals(2, dateWordFreqAl.size());
		assertEquals(new Integer(11), dateWordFreqAl.get(1).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(1).getString("word").toString().equals("testWord"));
		assertTrue(dateWordFreqAl.get(1).getDate("date").toString().equals("2016-05-09"));
	}

	@Test
	public void testFetchStringInt() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDb.save(dateStr, word, 13);
		derbyDb.save(dateStr, word + "a", 9);
		derbyDb.save(dateStr, word + "b", 11);

		// When
		ArrayList<Document> dateWordFreqAl = derbyDb.fetch("09-May-2016", 2);

		// then
		assertEquals(2, dateWordFreqAl.size());
		assertEquals(new Integer(9), dateWordFreqAl.get(1).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(1).getString("word").toString().equals("testWorda"));
		assertTrue(dateWordFreqAl.get(1).getDate("date").toString().equals("2016-05-09"));
	}

	@Test
	public void testSaveContentControl() {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";

		// When
		derbyDb.save(dateStr, word, 13);
		derbyDb.save(dateStr, word + "a", 9);
		derbyDb.save(dateStr, word + "b", 11);
		ArrayList<Document> dateWordFreqAl = derbyDb.fetch("09-May-2016");

		// then
		// First reg
		assertEquals(new Integer(9), dateWordFreqAl.get(0).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(0).getString("word").toString().equals("testWorda"));
		assertTrue(dateWordFreqAl.get(0).getDate("date").toString().equals("2016-05-09"));
		// Sec reg
		assertEquals(new Integer(11), dateWordFreqAl.get(1).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(1).getString("word").toString().equals("testWordb"));
		assertTrue(dateWordFreqAl.get(1).getDate("date").toString().equals("2016-05-09"));
		// third reg
		assertEquals(new Integer(13), dateWordFreqAl.get(2).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(2).getString("word").toString().equals("testWord"));
		assertTrue(dateWordFreqAl.get(2).getDate("date").toString().equals("2016-05-09"));
	}

	@Test
	public void testSaveSizeControl() {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";

		// When
		derbyDb.save(dateStr, word, 13);
		derbyDb.save(dateStr, word, 9);
		derbyDb.save(dateStr, word, 11);

		// then
		assertEquals(3, derbyDb.contain(dateStr, word));
	}

	@Test
	public void testTotalCount() {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";

		// When
		derbyDb.save(dateStr, word, 13);
		derbyDb.save(dateStr, word, 9);
		derbyDb.save(dateStr, word, 11);

		// then
		assertEquals(3, derbyDb.totalCount());

	}

	@Test
	public void testupdateCanIncrement() {
		// Given
		String date = "09-May-2016";
		String word = "testWord";
		// when
		derbyDb.save(date, word, 2);
		int frequencyLocal = Integer.parseInt(derbyDb.fetchFirstWDocument().get(2));
		derbyDb.update(date, word, 2);
		// then
		assertEquals(Integer.parseInt(derbyDb.fetchFirstWDocument().get(2)), frequencyLocal + 2);
	}

	@Test
	public void updateInvalidInput() {
		String date = "09-May-2016";
		assertFalse(derbyDb.update(date, "", 2));
	}

	@Test
	public void updateNullInput() {
		String date = "09-May-2016";
		assertFalse(derbyDb.update(date, null, 2));
	}

}
