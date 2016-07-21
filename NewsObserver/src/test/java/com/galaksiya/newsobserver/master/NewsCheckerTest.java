package com.galaksiya.newsobserver.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.galaksiya.newsobserver.database.DatabaseConstants;
import com.galaksiya.newsobserver.database.DatabaseFactory;
import com.galaksiya.newsobserver.database.MongoDb;
import com.galaksiya.newsobserver.master.DateUtils;
import com.galaksiya.newsobserver.master.NewsChecker;
import com.galaksiya.newsobserver.master.testutil.CreateRssJetty;
import com.galaksiya.newsobserver.parser.FeedMessage;
import com.galaksiya.newsobserver.parser.RssReader;

public class NewsCheckerTest {

	private static Server server;

	private static final int SERVER_PORT = 8111;

	@BeforeClass
	public static void startJetty() throws Exception {
		DatabaseFactory.getInstance().setDatabaseType(DatabaseConstants.DATABASE_TYPE_MONGO);
		server = new Server(SERVER_PORT);
		server.setHandler(new CreateRssJetty());
		server.setStopAtShutdown(true);
		server.start();
	}

	@AfterClass
	public static void stopJetty() throws Exception {
		DatabaseFactory.getInstance().setDefaultDatabaseType();
		server.stop();
	}

	private DateUtils dateUtils = new DateUtils();

	private NewsChecker newsChecker = new NewsChecker("test", new MongoDb("test"));

	private ArrayList<URL> rssLinksAL = new ArrayList<>();

	private RssReader rssReader = new RssReader();

	@Before
	public void before() throws MalformedURLException {
		rssLinksAL.add(new URL("http://localhost:" + SERVER_PORT + "/"));
		DatabaseFactory.setInstance(null);
		MongoDb mongoDbtest = new MongoDb("Test");
		mongoDbtest.delete();
		mongoDbtest = new MongoDb("newsTest");
		mongoDbtest.delete();
	}

	@Test
	public void dateCustomizeValidInput() {
		assertEquals("13 May 2016", dateUtils.dateCustomize("Fri May 13 10:24:56 EEST 2016"));
		assertEquals("22 Mar 2016", dateUtils.dateCustomize("Tue Mar 22 14:15:00 EET 2016"));
	}

	@Test
	public void handleMessage() {
		MongoDb mongoDb = new MongoDb("newsTest");
		mongoDb.delete();
		int sumOffreq = 0;
		FeedMessage messsage = createSampleMessage();
		DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
		databaseFactory.setDatabaseType("mongo");
		NewsChecker newsCheckerForNewTable = new NewsChecker("test", databaseFactory.getDatabase("newsTest"));
		Hashtable<String, Integer> wordFrequencyPerNew = newsCheckerForNewTable.handleMessage(messsage);
		Enumeration<String> e = wordFrequencyPerNew.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			// key: word - wordFrequencyPerNew.get(key):value
			sumOffreq += wordFrequencyPerNew.get(key);
		}
		assertEquals(60, sumOffreq);
	}

	@Test
	public void testcontainNewsTitleWInvalidTitle() {
		assertFalse(newsChecker.containNewsTitle("InvalidTitle", rssLinksAL.get(0)));
	}

	@Test
	public void testhandleMessageWInvalidDate(){
		DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
		databaseFactory.setDatabaseType("mongo");
		FeedMessage message=createSampleMessage();
		NewsChecker newsCheckerSpy = spy(new NewsChecker("test", databaseFactory.getDatabase("newsTest")));
		doReturn(false).when(newsCheckerSpy).traverseWordByWord(anyString(), any());
		assertEquals(null, newsCheckerSpy.handleMessage(message));
	}
	@Test
	public void titleControl() {
		// process each news...
		// can this function find a title
		assertTrue(newsChecker.containNewsTitle(getSampleTitle(), rssLinksAL.get(0)));
	}

	@Test
	public void traverseNewsWInvalidURL() {
		assertFalse(newsChecker.traverseNews(null));
	}

	@Test
	public void traverseNewsWValidURL() throws MalformedURLException {
		assertTrue(newsChecker.traverseNews(new URL("http://localhost:" + SERVER_PORT + "/")));
	}

	@Test
	public void traverseWordByWordWDatabaseProblemBODate() { // W:With
																// BO:BecauseOf
		assertFalse(newsChecker.traverseWordByWord("11 May 20161", createHashTableFortraverse(true)));
	}

	@Test
	public void traverseWordByWordWDatabaseProblemBOHashTable() { // W:With
																	// BO:BecauseOf
		assertFalse(newsChecker.traverseWordByWord("11 May 2016", createHashTableFortraverse(false)));
	}

	@Test
	public void traverseWordByWordWValidInputs() { // W:With
		assertTrue(newsChecker.traverseWordByWord("11 May 2016", createHashTableFortraverse(true)));
	}

	@Test
	public void updateActualNewsValidInput() {////
		Hashtable<String, String> lastNews = new Hashtable<>();
		lastNews.put("http://localhost:" + 8112 + "/", getSampleTitle());
		NewsChecker newsCheckerLocal = new NewsChecker(rssLinksAL);
		assertTrue(newsCheckerLocal.updateActualNews());
	}

	@Test
	public void updateActualNewsWNullList() {
		ArrayList<URL> RssLinksAl = null;
		NewsChecker newsCheckerWNullArrayList = new NewsChecker(RssLinksAl);
		assertFalse(newsCheckerWNullArrayList.updateActualNews());
	}

	private Hashtable<String, Integer> createHashTableFortraverse(boolean correct) {
		Hashtable<String, Integer> wordFrequencyPerNew = new Hashtable<>();
		if (correct) {
			wordFrequencyPerNew.put("test", 2);
		} else {
			wordFrequencyPerNew.put("test", -1);
		}
		return wordFrequencyPerNew;
	}

	private FeedMessage createSampleMessage() {
		FeedMessage messsage = new FeedMessage();
		messsage.setTitle("Erdoğan: Döviz rezervleri 150-165 milyar dolar olmalı");
		messsage.setDescription(
				"Cumhurbaşkanı Tayyip Erdoğan, Kocaeli Üniversitesinde toplu açılış ve fahri doktora töreninde yaptığı açıklamalarda, 27.5 milyar dolar döviz rezervi olan bir Merkez Bankası vardı. Şu anda 113 milyar dolar. Görevi bıraktığımda aslında 136 milyar dolara kadar yükselmişti ancak krizler falan şu anda 113 milyar dolar dedi  ve ekledi: Ama yeniden inanıyorum ki 136 milyar dolar da");
		messsage.setLink("http://www.birgun.net/haber-detay/kpss-sonuclari-aciklandi-119916.html");
		messsage.setPubDate("Mon May 02 20:03:40 EEST 2016");
		return messsage;
	}

	private String getSampleTitle() {
		return rssReader.parseFeed(rssLinksAL.get(0)).get(0).getTitle();
	}
}