package com.galaksiya.newsObserver.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsobserver.database.Database;
import com.galaksiya.newsobserver.database.MongoDb;
import com.galaksiya.newsobserver.master.DateUtils;
import com.galaksiya.newsobserver.parser.FeedMessage;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class MongoDbTest {
	private static final String LOCALHOST = "localhost";
	private static final Logger LOG = Logger.getLogger(MongoDbTest.class);
	private static final String MONGO_DB_NAME = "mydb";
	private String COLLECTION_NAME = "test";
	private String COLLECTION_NAME_NEWS = "newsTest";
	private String date;
	private DateUtils dateUtils = new DateUtils();
	private MongoClient mongoClient;
	private Database mongoDb = new MongoDb("test");
	private String word;

	@After
	public void After() {
		MongoDb mongoDb = new MongoDb("test");
		mongoDb.delete();
		MongoDb mongoDbNews = new MongoDb("newsTest");
		mongoDbNews.delete();
	}

	@Before
	public void before() {
		mongoDb = new MongoDb("test");
		mongoDb.delete();
		word = "test";
		date = "17-May-2016";
		mongoClient = getInstance();
		getCollection("test");
	}

	@Test
	public void canUpdate() {
		mongoDb.save(date, word, 2);
		assertTrue(mongoDb.update(date, word, 2));
	}

	public long contain(String dateStr, String word) {
		if (word == null || word.equals("")) {
			return -1;
		}
		Date date = dateUtils.dateConvert(dateStr);
		Bson filter = new Document().append("date", date).append("word", word);
		return getCollection(COLLECTION_NAME).count(filter);
	}

	@Test
	public void containInvalidInput() {
		assertEquals(-1, mongoDb.contain(date, ""));
	}

	@Test
	public void containNullInput() {
		assertEquals(-1, mongoDb.contain(date, null));
	}

	@Test
	public void containTest() {
		assertEquals(0, mongoDb.contain(date, word));
		save(date, word, 2);
		assertEquals(1, mongoDb.contain(date, word));
	}

	@Test
	public void delete() {
		save(date, word, 3);
		save("17 Mar 2015", "test2", 4);
		assertEquals(2, totalCount(COLLECTION_NAME));
		assertTrue(mongoDb.delete());
		assertEquals(0, totalCount(COLLECTION_NAME));
	}

	public ArrayList<String> fetchFirstWDocument() {
		try {
			ArrayList<String> date_word_freq = new ArrayList<String>();
			FindIterable<Document> search = getCollection(COLLECTION_NAME).find();
			if (search.first() == null) {
				return null;
			}
			date_word_freq.add(search.first().get("date").toString());
			date_word_freq.add(search.first().get("word").toString());
			date_word_freq.add(search.first().get("frequency").toString());
			return date_word_freq;
		} catch (Exception e) {
			LOG.error("Can't fetch the first document.", e);
		}
		return null;
	}

	@Test
	public void fetchFirstWDOcument() {
		ArrayList<String> firstDoc = (ArrayList<String>) mongoDb.fetchFirstWDocument();
		if (firstDoc != null)
			fail("fetchFirstDocument test has broken because of :" + firstDoc.get(0) + "\t" + firstDoc.get(1) + "\t"
					+ firstDoc.get(2) + "\t in added database");
		save(date, word, 2);
		firstDoc = (ArrayList<String>) mongoDb.fetchFirstWDocument();
		assertFalse(firstDoc.isEmpty());
	}

	public ArrayList<Document> findIterableToArraylist(FindIterable<Document> iterable) {
		ArrayList<Document> dataAL = new ArrayList<Document>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			dataAL.add(cursor.next());
		}
		return dataAL;
	}

	public MongoCollection<Document> getCollection(String collectionName) {
		return mongoClient.getDatabase(MONGO_DB_NAME).getCollection(collectionName);
	}

	public MongoClient getInstance() {
		MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
		builder.connectionsPerHost(100);// TODO 100 u field yap
		mongoClient = new MongoClient(new ServerAddress(LOCALHOST), builder.build());
		return mongoClient;
	}

	public ArrayList<Document> getNews() {
		try {
			FindIterable<Document> iterable = getCollection(COLLECTION_NAME_NEWS).find();
			ArrayList<Document> newsAl = findIterableToArraylist(iterable);
			return newsAl;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return null;
	}

	@Test
	public void getNewsContentControl() {
		DateUtils dateUtils = new DateUtils();
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		saveNews(message);
		ArrayList<Document> newsAL = mongoDb.getNews();
		if (newsAL == null || newsAL.size() < 1)
			fail("Couldn't fetch.");
		Document document = newsAL.get(0);
		boolean isTitleDescPubDateEqualsWithDatabase = document.get("title").equals(message.getTitle())
				&& document.get("description").equals(message.getDescription());
		assertEquals(dateUtils.dateCustomize(document.get("pubDate").toString()),
				dateUtils.dateCustomize("Mon May 02 00:00:00 EEST 2016"));
		assertTrue(isTitleDescPubDateEqualsWithDatabase);
	}

	@Test
	public void getNewsSizeControl() {
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		saveNews(message);
		ArrayList<Document> newsAL = mongoDb.getNews();
		if (newsAL == null || newsAL.size() < 1)
			fail("Couldn't fetch.");
		assertEquals(1, newsAL.size());
	}

	@Test
	public void overrideFetch() {
		for (int i = 0; i < 12; i++) {
			save(date, word, 2);
		}
		assertEquals(totalCount(COLLECTION_NAME), (mongoDb.fetch().size()));
	}

	@Test
	public void overrideFetchDate() {
		save(date, word, 2);
		save(date, word, 2);
		save(date, word, 2);
		save(date, word, 2);
		assertEquals(contain(date, word), mongoDb.fetch(date).size());
	}

	@Test
	public void overrideFetchDateLimit() {
		save(date, word, 2);
		save(date, word, 2);
		save(date, word, 2);
		save(date, word, 2);
		assertEquals(2, mongoDb.fetch(date, 2).size());
	}

	@Test
	public void save() {
		assertEquals(0, contain(date, word));
		assertTrue(mongoDb.save(date, word, 2));
		assertEquals(1, contain(date, word));
	}

	public boolean save(String dateStr, String word, int frequency) {
		if (word == null || word.equals("")) {
			return false;
		}
		try {
			Date date = dateUtils.dateConvert(dateStr);
			getCollection(COLLECTION_NAME)
					.insertOne(new Document().append("date", date).append("word", word).append("frequency", frequency));
			return true;

		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return false;
	}

	@Test
	public void saveInvalidInput() {
		assertFalse(mongoDb.save(date, "", 2));
	}

	@Test
	public void saveNewInsertDataControl() { ////////////////////////////
		DateUtils dateUtils = new DateUtils();
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		mongoDb.saveNews(message);
		ArrayList<Document> newsAL = getNews();
		if (newsAL == null || newsAL.size() < 1)
			fail("Couldn't insert.");
		Document document = newsAL.get(0);
		boolean isTitleDescPubDateEqualsWithDatabase = document.get("title").equals(message.getTitle())
				&& document.get("description").equals(message.getDescription());
		assertEquals(dateUtils.dateCustomize(document.get("pubDate").toString()),
				dateUtils.dateCustomize("Mon May 02 00:00:00 EEST 2016"));
		assertTrue(isTitleDescPubDateEqualsWithDatabase);
	}

	public boolean saveNews(FeedMessage message) {
		if (message == null || message.getTitle() == null || message.getTitle().equals("")
				|| message.getDescription() == null || message.getDescription().equals("")
				|| message.getpubDate() == null || message.getpubDate().equals("")) {
			return false;
		}
		try {
			Date date = dateUtils.dateConvert(dateUtils.dateCustomize(message.getpubDate()));
			getCollection(COLLECTION_NAME_NEWS).insertOne(
					new Document().append("title", message.getTitle()).append("description", message.getDescription())
							.append("link", message.getLink()).append("pubDate", date));
			return true;
		} catch (MongoWriteException e) {
			LOG.error("Data couldn't be inserted.", e);
		}
		return false;
	}

	@Test
	public void saveNewscanInsert() {
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		mongoDb.saveNews(message);
		assertEquals(1, totalCount(COLLECTION_NAME_NEWS));
	}

	@Test
	public void saveNewsEmptyDescription() {
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}

	@Test
	public void saveNewsEmptyPubDate() {
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("");
		assertFalse(mongoDb.saveNews(message));
	}

	@Test
	public void saveNewsEmptyTitle() {
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}

	@Test
	public void saveNewsNullDescription() {
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(null);
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}

	@Test
	public void saveNewsNullPubdate() {
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate(null);
		assertFalse(mongoDb.saveNews(message));
	}

	@Test
	public void saveNewsNullTitle() {
		MongoDb mongoDb = new MongoDb("newsTest");
		FeedMessage message = new FeedMessage();
		message.setTitle(null);
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mongoDb.saveNews(message));
	}

	@Test
	public void saveNullInput() {
		assertFalse(mongoDb.save(date, null, 2));
	}

	@Test
	public void testExistWInvalidTableName() {
		MongoDb mockedMongoDb = mock(MongoDb.class);
		when(mockedMongoDb.getCollection(any())).thenReturn(null);
		assertFalse(mockedMongoDb.delete());
	}

	@Test
	public void testSaveNewsWInvalidTableName() {
		MongoDb mockedMongoDb = mock(MongoDb.class);
		when(mockedMongoDb.getCollection(any())).thenReturn(null);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(mockedMongoDb.saveNews(message));
	}

	@Test
	public void testSaveWInvalidTableName() {
		MongoDb mockedMongoDb = mock(MongoDb.class);
		when(mockedMongoDb.getCollection(any())).thenReturn(null);
		assertFalse(mockedMongoDb.save(date, word, 2));
	}

	@Test
	public void testUpdateWInvalidTableName() {
		MongoDb mockedMongoDb = mock(MongoDb.class);
		when(mockedMongoDb.getCollection(any())).thenReturn(null);
		assertFalse(mockedMongoDb.update(date, word, 2));
	}

	public long totalCount(String collectionName) {
		return getCollection(collectionName).count(new Document());
	}

	@Test
	public void totalCountTest() {
		assertEquals(0, mongoDb.totalCount());
		for (int i = 0; i < 6; i++) {
			save(date, word, 2);
		}
		assertEquals(6, mongoDb.totalCount());
	}
	@Test
	public void updateCanIncrement() {
		save(date, word, 2);
		int frequencyLocal = Integer.parseInt(fetchFirstWDocument().get(2));
		mongoDb.update(date, word, 2);
		assertEquals(Integer.parseInt(fetchFirstWDocument().get(2)), frequencyLocal + 2);
	}
	@Test
	public void updateInvalidInput() {
		assertFalse(mongoDb.update(date, "", 2));
	}
	@Test
	public void updateNullInput() {
		assertFalse(mongoDb.update(date, null, 2));
	}
}
