package com.galaksiya.newsObserver.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.galaksiya.newsObserver.master.testutil.CreateRssJetty;
import com.galaksiya.newsObserver.parser.FeedMessage;
import com.galaksiya.newsObserver.parser.RssReader;

public class NewsCheckerTest {

	private static Server server;

	private static final int SERVER_PORT = 8112;
	

	@BeforeClass
	public static void startJetty() throws Exception {
		server = new Server(SERVER_PORT);
		server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class)
				.setHttpCompliance(HttpCompliance.LEGACY);
		server.setHandler(new CreateRssJetty());
		server.setStopAtShutdown(true);
		server.start();
	}

	@AfterClass
	public static void stopJetty() throws Exception {
		server.stop();
	}

	private NewsChecker newsChecker = new NewsChecker();

	private ArrayList<URL> rssLinksAL = new ArrayList<URL>();

	private RssReader rssReader = new RssReader();

	@Test
	public void canConvert() {
		assertTrue(newsChecker.canConvert("02-May-2016"));
		assertTrue(newsChecker.canConvert("02-May 2016"));
		assertTrue(newsChecker.canConvert("02 May 2016"));
	}
	@Test
	public void canConvertInvalidInput() {
		assertFalse(newsChecker.canConvert("02 May3 2016"));
	}
	@Test 
	public void canConvertWithoutBlankInput(){
		assertFalse(newsChecker.canConvert("21 May!2011"));
	}
	@Test
	public void dateCustomizeValidInput() {
		assertEquals("13 May 2016", newsChecker.dateCustomize("Fri May 13 10:24:56 EEST 2016"));
		assertEquals("22 Mar 2016", newsChecker.dateCustomize("Tue Mar 22 14:15:00 EET 2016"));
	}

	@Test
	public void handleMessage() {
		int sumOffreq = 0;
		FeedMessage messsage = createSampleMessage();
		Hashtable<String, Integer> wordFrequencyPerNew = newsChecker.handleMessage(messsage);
		Enumeration<String> e = wordFrequencyPerNew.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			// key: word - wordFrequencyPerNew.get(key):value
			sumOffreq += wordFrequencyPerNew.get(key);
		}
		assertEquals(62, sumOffreq);
	}

	@Before
	public void before() throws Exception {
		rssLinksAL.add(new URL("http://localhost:" + SERVER_PORT + "/"));
	}
	
	@Test
	public void titleControl() {
		// process each news...
		// can this function find a title
		assertTrue(newsChecker.containNewsTitle(getSampleTitle(), rssLinksAL.get(0)));
	}

	private String getSampleTitle() {
		return rssReader.parseFeed(rssLinksAL.get(0)).get(0).getTitle();
	}

	@Test
	public void updateActualNewsNullInput() {
		assertFalse(newsChecker.updateActualNews(null));
	}

	@Test
	public void updateActualNewsValidInput() {
		Hashtable<String, String> lastNews = new Hashtable<String, String>();
		lastNews.put("http://localhost:"+8112+"/", getSampleTitle());
		assertTrue(newsChecker.updateActualNews(rssLinksAL));
	}

	private FeedMessage createSampleMessage() {
		FeedMessage messsage = new FeedMessage();
		messsage.setTitle("Erdoğan: Döviz rezervleri 150-165 milyar dolar olmalı");
		messsage.setDescription(
				"Cumhurbaşkanı Tayyip Erdoğan, Kocaeli Üniversitesinde toplu açılış ve fahri doktora töreninde yaptığı açıklamalarda, 27.5 milyar dolar döviz rezervi olan bir Merkez Bankası vardı. Şu anda 113 milyar dolar. Görevi bıraktığımda aslında 136 milyar dolara kadar yükselmişti ancak krizler falan şu anda 113 milyar dolar dedi  ve ekledi: Ama yeniden inanıyorum ki 136 milyar dolar da");
		messsage.setPubDate("Mon May 02 20:03:40 EEST 2016");
		return messsage;
	}
}