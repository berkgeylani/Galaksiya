package com.galaksiya.newsobserver.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsobserver.master.DateUtils;
import com.galaksiya.newsobserver.parser.FeedMessage;
/**
 * It is a database class which is using Derby Db.
 * @author francium
 *
 */
public class DerbyDb implements Database {

	private static final String DATABASE_NAME = "Db.db";

	private static final Logger LOG = Logger.getLogger(DerbyDb.class);

	private static DerbyDb DERBY_DB_INSTANCE = null;

	private static Object instanceLock = new Object();

	/**
	 * Fabric of a DerbyDb
	 * 
	 * @return a DerbyDb which parameters are "STATISTICS","Db.db"
	 *         (tableName,database).
	 */
	public static DerbyDb getInstance() {
		if (DERBY_DB_INSTANCE == null) {
			synchronized (instanceLock) {
				if (DERBY_DB_INSTANCE == null) { // yes double check
					DERBY_DB_INSTANCE = new DerbyDb();
				}
			}
		}
		return DERBY_DB_INSTANCE;
	}

	private Connection conn = null;

	private DateUtils dateUtils = new DateUtils();

	private String tableName = null;
	
	/**
	 * Default database_name : Db.db
	 * Default table_name : STATISTICS
	 */
	public DerbyDb() {
		this(DatabaseConstants.TABLE_NAME_STATISTICS);
	}
	/**
	 * Default database_name : Db.db
	 * @param tableName String to set table name
	 */
	public DerbyDb(String tableName) {
		this(tableName, DATABASE_NAME);
	}

	/**
	 * It provide us to select table name.
	 * 
	 * @param tableName
	 *            String to set table name.
	 * @param dbName
	 * 			  String to set database name.
	 * @throws SQLException
	 */
	public DerbyDb(String tableName, String dbName) {
		try {
			String DB_URL = "jdbc:derby:" + dbName + ";create=true";
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			conn = DriverManager.getConnection(DB_URL);
		} catch (Exception e) {
			LOG.error("Driver or connection database problem.", e);
		}
		if (tableName == null || tableName.isEmpty()) {
			this.tableName = "STATISTICS";
		} else {
			this.tableName = tableName.toUpperCase();
		}
		try {
			getCollection();
		} catch (SQLException e) {
			LOG.error("Cant create DerbyDb object.", e);
		}
	}

	@Override
	public long contain(Date date, String word) {
		if (word == null || "".equals(word)) {
			return -1;
		}
		int count = -1;
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		try (PreparedStatement selectCountemp = conn
				.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE word= ?  AND PUBLISHDATE= ? ")) {

			selectCountemp.setString(1, word);

			selectCountemp.setString(2, sqlDate.toString());
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				count = res.getInt(1);
			}
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.", e);
		}
		return count;
	}

	@Override
	public boolean delete() {
		try (PreparedStatement deleteEmp = conn.prepareStatement("DELETE FROM " + tableName)) {
			deleteEmp.execute();
			return true;
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.", e);
		}
		return false;
	}

	@Override
	public boolean exists(FeedMessage message) {
		if (!isMessageCorrect(message))
			return false;
		int count = 0;
		try (PreparedStatement findEmp = conn
				.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE LINK= ? ")) {
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

	@Override
	public List<Document> fetch() {
		return fetch(new Document(), new Document().append("frequency", 1), -1);
	}

	@Override
	public List<Document> fetch(Document find, Document sort, int limit) {
		ArrayList<Document> newsAl = new ArrayList<>();
		boolean search = false;
		java.sql.Date sqlDate = null;
		if (!find.isEmpty()) {
			sqlDate = new java.sql.Date(find.getDate("date").getTime());
			search = true;
		}
		String query = createQuery(sort, limit, search);
		try (PreparedStatement selectEmp = conn.prepareStatement(query)) {
			if (search) {
				selectEmp.setString(1, sqlDate.toString());
			}
			ResultSet res = selectEmp.executeQuery();
			while (res.next()) {
				Document document = new Document();
				document.append("date", res.getDate("PUBLISHDATE"));
				document.append("word", res.getString("WORD"));
				document.append("frequency", res.getInt("frequency"));
				newsAl.add(document);
			}
		} catch (SQLException e) {
			LOG.error("Sql exception while getting selected row/rows.)", e);
		}
		return newsAl;
	}

	@Override
	public List<Document> fetch(String date) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", 1), -1);
	}

	@Override
	public List<Document> fetch(String date, int limit) {
		Date dateConverted = dateUtils.dateConvert(date);
		return fetch(new Document().append("date", dateConverted), new Document().append("frequency", -1), limit);
	}

	@Override
	public List<String> fetchFirstWDocument() {
		ArrayList<String> dateWordFreq = new ArrayList<>();
		boolean oneTime = true;
		try (PreparedStatement selectCountemp = conn.prepareStatement("SELECT * FROM " + tableName)) {
			ResultSet res = selectCountemp.executeQuery();
			while (res.next() && oneTime) {
				dateWordFreq.add(res.getDate("PUBLISHDATE").toString());
				dateWordFreq.add(res.getString("WORD"));
				dateWordFreq.add(Integer.toString(res.getInt("frequency")));
				oneTime = false;
			}
		} catch (SQLException e) {
			LOG.error("Sql exception while getting first selected row.)", e);
		}
		return dateWordFreq;
	}

	/**
	 * It create tables with selected table name(Default: STATISTICS).It set
	 * when initialize DerbyDb.
	 * 
	 * @param client
	 *            Get a tableName
	 * @return boolean: true: Success. false : Failed.
	 * @throws SQLException
	 */
	public void getCollection() throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
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

	@Override
	public List<Document> getNews() {
		ArrayList<Document> newsAl = new ArrayList<>();
		try (PreparedStatement selectCountemp = conn.prepareStatement("SELECT * FROM " + tableName)) {
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				Document document = new Document();
				document.append("pubDate", res.getDate("PUBLISHDATE").toString());
				document.append("title", res.getString("TITLE"));
				document.append("description", res.getString("DESCRIPTION"));
				document.append("link", res.getString("LINK"));
				newsAl.add(document);
			}
		} catch (SQLException e) {
			LOG.error("Sql exception while getting first selected row.)", e);
		}
		return newsAl;
	}

	@Override
	public boolean save(String dateStr, String word, int frequency) {
		if (dateStr == null || word == null || "".equals(dateStr) || "".equals(word)) {
			return false;
		}
		try (PreparedStatement insertemp = conn
				.prepareStatement("insert into " + tableName + "(PUBLISHDATE,WORD,FREQUENCY) values(?,?,?)")) {
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

	@Override
	public boolean saveNews(FeedMessage message) {
		if (!isMessageCorrect(message)) {
			return false;
		}
		try (PreparedStatement insertemp = conn.prepareStatement(
				"insert into " + tableName + "(PUBLISHDATE,TITLE,LINK,DESCRIPTION) values(?,?,?,?)")) {
			java.sql.Date sqlDate = new java.sql.Date(
					dateUtils.dateConvert(dateUtils.dateCustomize(message.getpubDate())).getTime());
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

	/**
	 * It controls the message like is it null or is its content contain
	 * anything or null.
	 * 
	 * @param message
	 *            message(title description pubdate)
	 * @return true : Correct Message false: Incorrect message
	 */
	public boolean isMessageCorrect(FeedMessage message) {
		if (message == null) {
			return false;
		}
		boolean isMessagetitleInvalid = message.getTitle() == null || "".equals(message.getTitle());
		boolean isMessageDescInvalid = message.getDescription() == null || "".equals(message.getDescription());
		boolean isMessagePubDInvalid = message.getpubDate() == null || "".equals(message.getpubDate());
		if (isMessagetitleInvalid || isMessageDescInvalid || isMessagePubDInvalid) {
			return false;
		}
		return true;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.galaksiya.newsObserver.database.Database#totalCount()
	 */
	@Override
	public long totalCount() {
		int count = -1;
		try (PreparedStatement selectCountemp = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName)) {
			ResultSet res = selectCountemp.executeQuery();
			while (res.next()) {
				count = res.getInt(1);
			}
		} catch (SQLException e) {
			LOG.error("Row count can't be selected.", e);
		}
		return count;
	}

	@Override
	public boolean update(String dateStr, String word, int frequencyInc) {
		if (word == null || word.isEmpty() || dateStr == null || !dateUtils.canConvert(dateUtils.dateCustomize(dateStr)) || frequencyInc < 0) { 
			return false; 
		} 
		java.sql.Date sqlDate = new java.sql.Date(dateUtils.dateConvert(dateUtils.dateCustomize(dateStr)).getTime());
		try (PreparedStatement insertemp = conn.prepareStatement("UPDATE " + tableName + " SET FREQUENCY = "
				+ getFrequency(word, sqlDate) + "+" + frequencyInc + " WHERE word= ?  AND PUBLISHDATE= ? ");) {
			insertemp.setString(1, word);
			insertemp.setString(2, sqlDate.toString());
			insertemp.executeUpdate();
			return true;
		} catch (SQLException sqlExcept) {
			LOG.error("Data couldn't be updated.", sqlExcept);
		}
		return false;
	}

	private String createQuery(Document sort, int limit, boolean search) {
		String query = "SELECT * FROM " + tableName;
		if (search)
			query += " WHERE PUBLISHDATE = ? ";
		query += sortCase((int) sort.get("frequency"));
		if (limit > 0)
			query += " FETCH NEXT " + limit + " ROWS ONLY ";
		return query;
	}

	private int getFrequency(String word, java.sql.Date sqlDate) {
		int freq = 0;
		try (PreparedStatement getFrequency = conn
				.prepareStatement("SELECT FREQUENCY FROM " + tableName + " WHERE word= ?  AND PUBLISHDATE= ? ")) {
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

	private String sortCase(int sortKey) {
		return (sortKey > 0) ? "  ORDER BY FREQUENCY" : "";
	}

	@Override
	public boolean saveMany(List<Document> insertList) {
		boolean isSuccessful = false;
		if (insertList == null || insertList.isEmpty()) {
			return isSuccessful;
		}
		int i = 0;
		String sqlQuery = "insert into " + tableName + "(PUBLISHDATE,WORD,FREQUENCY) values(?,?,?)";
		sqlQuery = sqlQuery + new String(new char[insertList.size() - 1]).replace("\0", ",(?,?,?)");
		PreparedStatement insertemp;
		try {
			insertemp = conn.prepareStatement(sqlQuery);
			for (Document doc : insertList) {
				java.sql.Date sqlDate = new java.sql.Date(doc.getDate("date").getTime());
				insertemp.setString(i + 1, sqlDate.toString());
				insertemp.setString(i + 2, doc.getString("word"));
				insertemp.setInt(i + 3, doc.getInteger("frequency"));
				i += 3;
			}
			insertemp.executeUpdate();
			isSuccessful = true;
		} catch (SQLException e) {
			LOG.error("Data couldn't be inserted(Many).", e);
		}
		return isSuccessful;
	}

}