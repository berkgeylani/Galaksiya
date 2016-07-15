package com.galaksiya.newsObserver.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsObserver.master.DateUtils;
import com.galaksiya.newsObserver.parser.FeedMessage;

public class DerbyDb implements Database {

	private static Connection conn = null;

	private static final Logger LOG = Logger.getLogger(DerbyDb.class);

	private final static String DATABASE_NAME = "Db.db";

	private static Object instanceLock = new Object();
	
	/**
	 * Fabric of a MongoClient
	 * 
	 * @return a MongoClient which parameters are "localhost",27017
	 *         (dbAdress,port).
	 */
	public static Connection getInstance() {
		if (conn == null) {
			synchronized (instanceLock) {
				if (conn == null) { // yes double check
					// setting for conn will be here
					try {
						String DB_URL = "jdbc:derby:" + DATABASE_NAME + ";create=true";
						Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
						conn = DriverManager.getConnection(DB_URL);
					} catch (Exception except) {
						except.printStackTrace();
					}
				}
			}
		}
		return conn;
	}

	private DateUtils dateUtils = new DateUtils();

	private String tableName = DatabaseConstants.TABLE_NAME_STATISTICS;

	public DerbyDb() {
		try {
			getCollection();
		} catch (SQLException e) {
			LOG.error("Cant create DerbyDb onject.", e);
		}
	}

	/**
	 * It provide us to select table name.
	 * 
	 * @param collectionName
	 *            String to set table name.
	 * @throws SQLException
	 */
	public DerbyDb(String tableName) {
		if (tableName != null) {
			this.tableName = tableName.toUpperCase();
		}
		try {
			getCollection();
		} catch (SQLException e) {
			LOG.error("Cant create DerbyDb onject.", e);
		}
	}

	@Override
	public long contain(String dateStr, String word) {
		if (word == null || word.equals("")) {
			return -1;
		}
		int count = 0;
		java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateStr).getTime());
		PreparedStatement selectCountemp;
		try {
			selectCountemp = getInstance()
					.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE word= ?  AND PUBLISHDATE= ? ");
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

	@Override
	public boolean delete() {
		try {
			PreparedStatement DeleteEmp = getInstance().prepareStatement("DELETE FROM " + tableName);
			DeleteEmp.execute();
			return true;
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.");
		}
		return false;
	}

	@Override
	public ArrayList<Document> fetch() {
		return fetch(new Document(), new Document().append("frequency", 1), -1);
	}

	@Override
	public ArrayList<Document> fetch(Document find, Document sort, int limit) {
		ArrayList<Document> newsAl = new ArrayList<Document>();
		boolean search = false;
		java.sql.Date sqlDate = null;
		if (!find.isEmpty()) {
			sqlDate = new java.sql.Date(find.getDate("date").getTime());
			search = true;
		}
		String query = createQuery(sort, limit, search);
		try {
			PreparedStatement selectEmp;
			if (search) {
				selectEmp = getInstance().prepareStatement(query);

				selectEmp.setString(1, sqlDate.toString());
			} else {
				selectEmp = getInstance().prepareStatement(query);
			}
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

	public String createQuery(Document sort, int limit, boolean search) {
		String query = "SELECT * FROM " + tableName;
		if (search)
			query += " WHERE PUBLISHDATE = ? ";
		query += sortCase((int) sort.get("frequency"));
		if (limit > 0)
			query += " FETCH NEXT " + limit + " ROWS ONLY ";
		return query;
	}

	@Override
	public ArrayList<Document> fetch(String date) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", 1), -1);
	}

	@Override
	public ArrayList<Document> fetch(String date, int limit) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", -1), limit);
	}

	@Override
	public ArrayList<String> fetchFirstWDocument() {
		PreparedStatement selectCountemp;
		ArrayList<String> date_word_freq = new ArrayList<String>();
		try {
			selectCountemp = getInstance().prepareStatement("SELECT * FROM " + tableName);
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				date_word_freq.add(res.getDate("PUBLISHDATE").toString());
				date_word_freq.add(res.getString("WORD"));
				date_word_freq.add(res.getInt("frequency") + "");
				return date_word_freq;
			}
		} catch (SQLException e) {
			LOG.error("Sql exception while getting first selected row.)");
		}
		return null;
	}

	/**
	 * It returns MongoCollection with selected collection name(Default:
	 * statistics).It set when initialize MongoDb.
	 * 
	 * @param client
	 *            Get a MongoClient which is set "localhost", 27017.
	 * @return collection : Default: initialize statistics collection. You can
	 *         set when create MongoDb(//here).
	 * @throws SQLException
	 */
	public void getCollection() throws SQLException {
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
			} else {

				String statement = "CREATE TABLE " + tableName + "(" + "ID int NOT NULL GENERATED ALWAYS AS IDENTITY"
						+ "(START WITH 1,INCREMENT BY 1)," + "PUBLISHDATE DATE NOT NULL,"
						+ "WORD varchar(255) NOT NULL," + "FREQUENCY INT NOT NULL," + "PRIMARY KEY(ID)" + ")";
				stmt.execute(statement);
			}
		}
	}

	private int getFrequency(String word, java.sql.Date sqlDate) {
		int freq = 0;
		try {
			PreparedStatement getFrequency = getInstance()
					.prepareStatement("SELECT FREQUENCY FROM " + tableName + " WHERE word= ?  AND PUBLISHDATE= ? ");
			getFrequency.setString(1, word);
			getFrequency.setString(2, sqlDate.toString());
			ResultSet res = getFrequency.executeQuery();
			while (res.next()) {
				freq = res.getInt("FREQUENCY");
			}
		} catch (SQLException sqlExcept) {
			LOG.error("Data couldn't be selected.(Frequency)", sqlExcept);
		}
		return freq;
	}

	@Override
	public ArrayList<Document> getNews() {
		PreparedStatement selectCountemp;
		ArrayList<Document> newsAl = new ArrayList<Document>();
		try {
			selectCountemp = getInstance().prepareStatement("SELECT * FROM " + tableName);
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

	@Override
	public boolean save(String dateStr, String word, int frequency) {
		if (word == null || word.equals("")) {
			return false;
		}
		try {
			java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateStr).getTime());
			PreparedStatement insertemp = conn
					.prepareStatement("insert into " + tableName + "(PUBLISHDATE,WORD,FREQUENCY) values(?,?,?)");
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

	@Override
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
					"insert into " + tableName + "(PUBLISHDATE,TITLE,LINK,DESCRIPTION) values(?,?,?,?)");
			insertemp.setString(1, sqlDate.toString());
			insertemp.setString(2, message.getTitle().toString());
			insertemp.setString(3, message.getLink().toString());
			insertemp.setString(4, message.getDescription().toString());
			insertemp.executeUpdate();
			return true;
		} catch (SQLException sqlException) {
			LOG.error("Data couldn't be inserted.", sqlException);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#totalCount()
	 */
	@Override
	public long totalCount() {
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

	@Override
	public boolean update(String dateStr, String word, int frequencyInc) {
		if (word == null || word.isEmpty() || dateStr == null || !dateUtils.canConvert(dateStr)) {
			return false;
		}
		java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateStr).getTime());
		try {

			PreparedStatement insertemp = getInstance().prepareStatement("UPDATE " + tableName + " SET FREQUENCY = "
					+ getFrequency(word, sqlDate) + "+" + frequencyInc + " WHERE word= ?  AND PUBLISHDATE= ? ");
			insertemp.setString(1, word);
			insertemp.setString(2, sqlDate.toString());
			insertemp.executeUpdate();
			return true;
		} catch (SQLException sqlExcept) {
			LOG.error("Data couldn't be updated.", sqlExcept);
		}
		return false;
	}

	private String sortCase(int sortKey) {
		return (sortKey > 0) ? "  ORDER BY FREQUENCY" : "";
	}

	@Override
	public boolean exists(FeedMessage message) {
		if (message == null || message.getTitle() == null || message.getTitle().equals("")
				|| message.getDescription() == null || message.getDescription().equals("")
				|| message.getpubDate() == null || message.getpubDate().equals("")) {
			return false;
		}
		int count = 0;
		try {
			PreparedStatement findEmp = getInstance()
					.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE LINK= ? ");
			findEmp.setString(1, message.getLink());
			ResultSet rSet = findEmp.executeQuery();
			while (rSet.next()) {
				count = rSet.getInt(1);
			}
			if (count > 0) {
				return true;
			}
		} catch (SQLException e) {
			LOG.error("News find process have a trouble.", e);
		}

		return false;
	}

}