package com.galaksiya.newsObserver.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsObserver.master.DateUtils;
import com.galaksiya.newsObserver.parser.FeedMessage;

public class DerbyDbTest {
	private static final String TABLE_NAME_NEWS = "newsTest";
	private static final String TABLE_NAME = "TEST";
	private final static String DATABASE_NAME = "Db.db";
	private DateUtils dateUtils = new DateUtils();
	private Connection conn = null;
	private DerbyDb derbyDb = new DerbyDb(TABLE_NAME);
	private static final Logger LOG = Logger.getLogger(DerbyDbTest.class);

	@After
	public void after() {
		delete(TABLE_NAME);
		delete(TABLE_NAME_NEWS);
	}

	public Connection getInstance() {
		if (conn == null) {
			// setting for conn will be here
			try {
				String DB_URL = "jdbc:derby:" + DATABASE_NAME + ";create=true";
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
				conn = DriverManager.getConnection(DB_URL);
			} catch (Exception except) {
				except.printStackTrace();
			}
		}
		return conn;
	}

	public void getCollection(String tableName) throws SQLException {
		DatabaseMetaData dbmd = getInstance().getMetaData();
		ResultSet rs = dbmd.getTables(null, "APP", tableName, null);
		if (!(rs.next())) {
			Statement stmt = conn.createStatement();
			if (tableName.contains("NEWS")) {
				String statement = "CREATE TABLE " + tableName + "(" + "ID int NOT NULL GENERATED ALWAYS AS IDENTITY"
						+ "(START WITH 1,INCREMENT BY 1)," + "PUBLISHDATE DATE NOT NULL,"
						+ "TITLE varchar(600) NOT NULL," + "LINK varchar(200) NOT NULL,"
						+ "DESCRIPTION varchar(1500) NOT NULL," + "PRIMARY KEY(ID)" + ")";
				stmt.execute(statement);
			}
		}
		
		rs = dbmd.getTables(null, "APP", tableName, null);
		if (!(rs.next())) {
			Statement stmt = conn.createStatement();
			if (tableName.contains("TEST")) {

				String statement = "CREATE TABLE " + tableName + "(" + "ID int NOT NULL GENERATED ALWAYS AS IDENTITY"
						+ "(START WITH 1,INCREMENT BY 1)," + "PUBLISHDATE DATE NOT NULL,"
						+ "WORD varchar(255) NOT NULL," + "FREQUENCY INT NOT NULL," + "PRIMARY KEY(ID)" + ")";
				stmt.execute(statement);
			}
		}
	}

	public boolean delete(String tableName) {
		try {
			PreparedStatement DeleteEmp = getInstance().prepareStatement("DELETE FROM " + tableName);
			DeleteEmp.execute();
			return true;
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.");
		}
		return false;
	}

	public long contain(String dateStr, String word) {
		if (word == null || word.equals("")) {
			return -1;
		}
		int count = 0;
		java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateStr).getTime());
		PreparedStatement selectCountemp;
		try {
			selectCountemp = getInstance()
					.prepareStatement("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE word= ?  AND PUBLISHDATE= ? ");
			selectCountemp.setString(1, word);
			selectCountemp.setString(2, sqlDate.toString());
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				count = res.getInt(1);
			}
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.");
		}
		return count;
	}

	public ArrayList<Document> getNews() {
		PreparedStatement selectCountemp;
		ArrayList<Document> newsAl = new ArrayList<Document>();
		try {
			selectCountemp = getInstance().prepareStatement("SELECT * FROM " + TABLE_NAME_NEWS);
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				Document document = new Document();
				document.append("pubDate", (res.getDate("PUBLISHDATE").toString()));
				document.append("title", (res.getString("TITLE")));
				document.append("description", res.getString("DESCRIPTION"));
				document.append("link", res.getString("LINK"));
				newsAl.add(document);
			}
			return newsAl;
		} catch (SQLException e) {
			LOG.error("Sql exception while getting first selected row.)");
		}
		return null;
	}

	public long totalCount(String tableName) {
		int count = 0;
		PreparedStatement selectCountemp;
		try {
			selectCountemp = getInstance().prepareStatement("SELECT COUNT(*) FROM " + tableName);
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				count = res.getInt(1);
			}
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.");
		}
		return count;
	}

	public boolean save(String dateStr, String word, int frequency) {
		if (word == null || word.equals("")) {
			return false;
		}

		try {
			java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateStr).getTime());
			PreparedStatement insertemp = conn
					.prepareStatement("insert into " + TABLE_NAME + "(PUBLISHDATE,WORD,FREQUENCY) values(?,?,?)");
			insertemp.setString(1, sqlDate.toString());
			insertemp.setString(2, word);
			insertemp.setInt(3, frequency);
			insertemp.executeUpdate();
			return true;
		} catch (SQLException sqlExcept) {
			LOG.error("Data couldn't be inserted.", sqlExcept);
		}
		return false;
	}

	public ArrayList<Document> fetch(String date) {
		ArrayList<Document> newsAl = new ArrayList<Document>();
		java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(date).getTime());
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE PUBLISHDATE = ? ";
		try {
			PreparedStatement selectEmp;
			selectEmp = getInstance().prepareStatement(query);
			selectEmp.setString(1, sqlDate.toString());
			ResultSet res = selectEmp.executeQuery();
			while (res.next()) {
				Document document = new Document();
				document.append("date", (res.getDate("PUBLISHDATE")));
				document.append("word", (res.getString("WORD")));
				document.append("frequency", res.getInt("frequency"));
				newsAl.add(document);
			}
			return newsAl;
		} catch (SQLException e) {
			LOG.error("Sql exception while getting selected row/rows.)", e);
		}
		return null;
	}

	@Before
	public void before() throws SQLException {
		getCollection(TABLE_NAME);
		getCollection(TABLE_NAME_NEWS);
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
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		delete(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		derbyDbForNews.saveNews(message);
		ArrayList<Document> newsAL = getNews();
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
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		delete(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		derbyDbForNews.saveNews(message);
		assertEquals(1, totalCount(TABLE_NAME_NEWS));
	}

	@Test
	public void saveNewsEmptyDescription() {
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription("");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsEmptyPubDate() {
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsEmptyTitle() {
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsNullDescription() {
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(null);
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsNullPubdate() {
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate(null);
		assertFalse(derbyDbForNews.saveNews(message));
	}

	@Test
	public void saveNewsNullTitle() {
		// given
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle(null);
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
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
		save(date, word, 2);
		// then
		assertTrue(derbyDb.update(date, word, 2));
	}

	@Test
	public void testContain() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";

		// When
		save(dateStr, word, 9);

		// then
		assertEquals(1, derbyDb.contain(dateStr, word));
	}

	@Test
	public void testDelete() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		save(dateStr, word, 9);
		save(dateStr, word, 11);

		// When
		derbyDb.delete();

		// then
		assertEquals(0, contain(dateStr, word));
	}

	@Test
	public void testFetch() throws SQLException {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";
		save(dateStr, word, 9);
		save(dateStr, word, 11);

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
		save(dateStr, word, 13);
		save(dateStr, word + "a", 9);
		save(dateStr, word + "b", 11);

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
		save(dateStr, word, 9);
		save(dateStr, word, 11);
		save("03-May-2016", word, 11);

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
		save(dateStr, word, 13);
		save(dateStr, word + "a", 9);
		save(dateStr, word + "b", 11);

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
		ArrayList<Document> dateWordFreqAl = fetch("09-May-2016");

		// then
		// First reg
		assertEquals(new Integer(13), dateWordFreqAl.get(0).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(0).getString("word").toString().equals("testWord"));
		assertTrue(dateWordFreqAl.get(0).getDate("date").toString().equals("2016-05-09"));
		// Sec reg
		assertEquals(new Integer(9), dateWordFreqAl.get(1).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(1).getString("word").toString().equals("testWorda"));
		assertTrue(dateWordFreqAl.get(1).getDate("date").toString().equals("2016-05-09"));
		// third reg
		assertEquals(new Integer(11), dateWordFreqAl.get(2).getInteger("frequency"));
		assertTrue(dateWordFreqAl.get(2).getString("word").toString().equals("testWordb"));
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
		assertEquals(3, contain(dateStr, word));
	}

	@Test
	public void testTotalCount() {
		// Given
		String dateStr = "09-May-2016";
		String word = "testWord";

		// When
		save(dateStr, word, 13);
		save(dateStr, word, 9);
		save(dateStr, word, 11);

		// then
		assertEquals(3, derbyDb.totalCount());

	}

	@Test
	public void testupdateCanIncrement() {
		// Given
		String date = "09-May-2016";
		String word = "testWord";
		// when
		save(date, word, 2);
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
