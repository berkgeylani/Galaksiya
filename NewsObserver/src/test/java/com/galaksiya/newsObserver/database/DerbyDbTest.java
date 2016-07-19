package com.galaksiya.newsObserver.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsobserver.database.DerbyDb;
import com.galaksiya.newsobserver.master.DateUtils;
import com.galaksiya.newsobserver.parser.FeedMessage;

public class DerbyDbTest {
	private final static String DATABASE_NAME = "Db.db";
	private static final Logger LOG = Logger.getLogger(DerbyDbTest.class);
	private static final String TABLE_NAME = "TEST";
	private static final String TABLE_NAME_NEWS = "newsTest";
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	@AfterClass
	public static void shutDown() throws SQLException {
		Connection conn = null;
		try {
			String DB_URL = "jdbc:derby:" + DATABASE_NAME + ";create=true";
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			conn = DriverManager.getConnection(DB_URL);
		
		String[] tables = { TABLE_NAME, TABLE_NAME_NEWS };
		for (String table : tables) {
			try (Statement stmt = conn.createStatement()) {
				String statement = "DROP TABLE " + table + " ;";
				stmt.execute(statement);
			} catch (SQLException e) {
				LOG.error("Can't create table.", e);
			}
		}
		} catch (Exception except) {
			LOG.error("Driver or connection database problem.", except);
		}finally {
			conn.close();
		}
	}
	private Connection conn = null;

	private DateUtils dateUtils = new DateUtils();

	private DerbyDb derbyDb = new DerbyDb(TABLE_NAME);

	@After
	public void after() {
		delete(TABLE_NAME);
		delete(TABLE_NAME_NEWS);
	}

	@Before
	public void before() throws SQLException {
		getCollection(TABLE_NAME);
		getCollection(TABLE_NAME_NEWS);
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
			LOG.error("Row count can't be selected.",e);
		}
		return count;
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

	public boolean delete(String tableName) {
		if (tableName == null || tableName.isEmpty()) {
			return false;
		}
		try {
			PreparedStatement DeleteEmp = getInstance().prepareStatement("DELETE FROM " + tableName);
			DeleteEmp.execute();
			return true;
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.",e);
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

	public void getCollection(String tableName) throws SQLException {
		DatabaseMetaData dbmd = getInstance().getMetaData();
		ResultSet rs = dbmd.getTables(null, "APP", tableName, null);
		if (!(rs.next())) {
			try (Statement stmt = conn.createStatement()) {
				if (tableName.contains("NEWS")) {
					String statement = "CREATE TABLE " + tableName + "("
							+ "ID int NOT NULL GENERATED ALWAYS AS IDENTITY" + "(START WITH 1,INCREMENT BY 1),"
							+ "PUBLISHDATE DATE NOT NULL," + "TITLE varchar(600) NOT NULL,"
							+ "LINK varchar(200) NOT NULL," + "DESCRIPTION varchar(1500) NOT NULL," + "PRIMARY KEY(ID)"
							+ ")";
					stmt.execute(statement);
				}
			} catch (SQLException e) {
				LOG.error("CAnt create table.", e);
			}
		}

		rs = dbmd.getTables(null, "APP", tableName, null);
		if (!(rs.next())) {
			try (Statement stmt = conn.createStatement()) {
				if (tableName.contains("TEST")) {

					String statement = "CREATE TABLE " + tableName + "("
							+ "ID int NOT NULL GENERATED ALWAYS AS IDENTITY" + "(START WITH 1,INCREMENT BY 1),"
							+ "PUBLISHDATE DATE NOT NULL," + "WORD varchar(255) NOT NULL," + "FREQUENCY INT NOT NULL,"
							+ "PRIMARY KEY(ID)" + ")";
					stmt.execute(statement);
				}
			} catch (SQLException e) {
				LOG.error("CAnt create table.", e);
			}
		}
	}

	public Connection getInstance() {
		if (conn == null) {
			// setting for conn will be here
			try {
				String DB_URL = "jdbc:derby:" + DATABASE_NAME + ";create=true";
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
				conn = DriverManager.getConnection(DB_URL);
			} catch (Exception except) {
				LOG.error("Driver or connection database problem.", except);
			}
		}
		return conn;
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
			LOG.error("Sql exception while getting first selected row.)",e);
		}
		return null;
	}

	public boolean save(String dateStr, String word, int frequency) {
		if (word == null || word.equals("")) {
			return false;
		}

		try (PreparedStatement insertemp = conn
				.prepareStatement("insert into " + TABLE_NAME + "(PUBLISHDATE,WORD,FREQUENCY) values(?,?,?)")) {
			java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateStr).getTime());
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
		if (newsAL == null || newsAL.size() < 1)
			fail("Couldn't insert.");
		Document document = newsAL.get(0);
		boolean isTitleDescPubDateEqualsWithDatabase = document.get("title").equals(message.getTitle())
				&& document.get("description").equals(message.getDescription());
		assertEquals(document.get("pubDate").toString(), "2016-05-02");
		assertTrue(isTitleDescPubDateEqualsWithDatabase);
	}

	public boolean saveNews(FeedMessage message) {
		if (message == null || message.getTitle() == null || message.getTitle().equals("")
				|| message.getDescription() == null || message.getDescription().equals("")
				|| message.getpubDate() == null || message.getpubDate().equals("")) {
			return false;
		}
		try {
			java.sql.Date sqlDate = new java.sql.Date(
					dateUtils.dateConvert(dateUtils.dateCustomize(message.getpubDate())).getTime());
			PreparedStatement insertemp = getInstance().prepareStatement(
					"insert into " + TABLE_NAME_NEWS + "(PUBLISHDATE,TITLE,LINK,DESCRIPTION) values(?,?,?,?)");
			insertemp.setString(1, sqlDate.toString());
			insertemp.setString(2, message.getTitle());
			insertemp.setString(3, message.getLink());
			insertemp.setString(4, message.getDescription());
			insertemp.executeUpdate();
			return true;
		} catch (SQLException sqlException) {
			LOG.error("Data couldn't be inserted.", sqlException);
		}
		return false;
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
	public void testContainWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertEquals(-1, derbyDbInvalidTableName.contain("21 May 2016", "qwer"));
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
	public void testdeleteWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertFalse(derbyDbInvalidTableName.delete());
	}

	@Test
	public void TestDerbyDbConstructorCreateTrue() {
		// checkisthheredb? should be false
		File f = new File(System.getProperty("user.dir") + "/test.db");
		assertFalse(f.exists() && !f.isDirectory());
		DerbyDb derbyDb = new DerbyDb("TEST", "test.db");
		// checkistheredb? should be true
		assertTrue(f.exists() && f.isDirectory());
		// deletedb
		deleteDir(f);
	}

	@Test
	public void testExistWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		derbyDbInvalidTableName.setTableName(null);
		assertFalse( derbyDbInvalidTableName.exists(message));
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
	public void testfetchFirstWDocumentWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertEquals(null, derbyDbInvalidTableName.fetchFirstWDocument());
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
	public void testfetchStringStringIntWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertEquals(null, derbyDbInvalidTableName.fetch(new Document(), new Document().append("frequency", 1), -1));
	}

	@Test
	public void testfetchWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertEquals(null, derbyDbInvalidTableName.fetch());
	}

	@Test
	public void testGetNewsContent() {
		DerbyDb derbyDbForNews = new DerbyDb(TABLE_NAME_NEWS);
		delete(TABLE_NAME_NEWS);
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		saveNews(message);
		ArrayList<Document> newsAL = derbyDbForNews.getNews();
		assertTrue(newsAL.size() == 1);
		Document document = newsAL.get(0);
		boolean isTitleDescPubDateEqualWithDatabase = document.get("title").equals(message.getTitle())
				&& document.get("description").equals(message.getDescription());
		assertEquals(document.get("pubDate").toString(), "2016-05-02");
		assertTrue(isTitleDescPubDateEqualWithDatabase);
	}

	@Test
	public void testGetNewsWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertEquals(null, derbyDbInvalidTableName.getNews());
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
	public void testSaveNewsWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);	
		FeedMessage message = new FeedMessage();
		message.setTitle("FETÖ lideri saldırıdan sonra DAEŞ’i değil devleti suçladı!");
		message.setDescription(
				"Fetullahçı Terör Örgütü lideri Fetullah Gülen ihanet için hiçbir fırsatı kaçırmıyor. İstanbul Atatürk Hava Limanı’ndaki terör saldırısından sonra bir taziye yayınlayan Fetullahçı çetenin lideri, teröristleri...");
		message.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		message.setPubDate("Mon May 02 20:03:40 EEST 2016");
		assertFalse( derbyDbInvalidTableName.saveNews(message));
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
	public void testSaveWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDbInvalidTableName.setTableName(null);
		assertFalse( derbyDbInvalidTableName.save(dateStr, word, 9));
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
	public void testTotalCountWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		derbyDbInvalidTableName.setTableName(null);
		assertEquals(-1, derbyDbInvalidTableName.totalCount());
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
	public void testUpdateWInvalidTableName() {
		DerbyDb derbyDbInvalidTableName = new DerbyDb(TABLE_NAME);
		String dateStr = "09-May-2016";
		String word = "testWord";
		derbyDbInvalidTableName.setTableName(null);
		assertFalse( derbyDbInvalidTableName.update(dateStr, word, 9));
	}
	public long totalCount(String tableName) {
		if (tableName == null || tableName.isEmpty()) {
			return -1;
		}
		int count = 0;
		PreparedStatement selectCountemp;
		try {
			selectCountemp = getInstance().prepareStatement("SELECT COUNT(*) FROM " + tableName);
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				count = res.getInt(1);
			}
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.",e);
		}
		return count;
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
