package master;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import database.MongoDb;
import rssparser.FeedMessage;
import rssparser.RssReader;

public class NewsCheckerTest {
	/*
	 * database'e çevirilemeyecek bir date gönderebilirirmi
	 * 
	 * */
	private ArrayList<URL> rssLinksAL = new ArrayList<URL>();
	private NewsChecker newsChecker = new NewsChecker();
	private RssReader parserOfRss=new RssReader();
	private String title=null;
	private FeedMessage messsage = new FeedMessage();
	private static Server server = new Server(8112);//static yaptık çünkü classın initialize'dan edilmeden önce çalıştırılması gerekiyor.
	
	@BeforeClass
	public static void startJetty() throws Exception{
        server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class).setHttpCompliance(HttpCompliance.LEGACY);
        server.setHandler(new HelloHandler());
        server.setStopAtShutdown(true);
        server.start();
	}
	@Before
	public void setUp() throws Exception {
		rssLinksAL.add(new URL("http://localhost:8112/"));
		
		messsage.setTitle("Erdoğan: Döviz rezervleri 150-165 milyar dolar olmalı");
		messsage.setDescription(
				"Cumhurbaşkanı Tayyip Erdoğan, Kocaeli Üniversitesinde toplu açılış ve fahri doktora töreninde yaptığı açıklamalarda, 27.5 milyar dolar döviz rezervi olan bir Merkez Bankası vardı. Şu anda 113 milyar dolar. Görevi bıraktığımda aslında 136 milyar dolara kadar yükselmişti ancak krizler falan şu anda 113 milyar dolar dedi  ve ekledi: Ama yeniden inanıyorum ki 136 milyar dolar da");
		messsage.setPubDate("Mon May 02 20:03:40 EEST 2016");
		
	}
	@Test
	public void updateActualNewsNullInput() {
		assertFalse(newsChecker.updateActualNews(null, null));
	}
	
	@Test
	public void updateActualNewsValidInput() { // internet connection warning  çözüm : internet yoksa bakamsın bu teste?
		Hashtable<String, String> lastNews = new Hashtable<String, String>();
		String title=null;
		for (FeedMessage message : parserOfRss.feedParser(rssLinksAL.get(0))) {  //item to item reading
			title=message.getTitle();
		}
		lastNews.put("http://localhost:8112/", title);
		assertTrue(newsChecker.updateActualNews(rssLinksAL, lastNews));
	}
	@Test
	public void handlingControl() {
		int sumOffreq=0;
		Hashtable<String, Integer> wordFrequencyPerNew = newsChecker.messageHandling(messsage);
		Enumeration<String> e = wordFrequencyPerNew.keys();
		while (e.hasMoreElements()) { 
			String key = (String) e.nextElement();//key: word   wordFrequencyPerNew.get(key):value
			sumOffreq+=wordFrequencyPerNew.get(key);
		}
		assertEquals(62, sumOffreq);
	}
	@Test
	public void canConvertInputs() {
		assertTrue(newsChecker.canConvert("02-May-2016"));
		assertTrue(newsChecker.canConvert("02-May 2016"));
		assertTrue(newsChecker.canConvert("02 May 2016"));
		assertFalse(newsChecker.canConvert("02 May3 2016"));
		assertFalse(newsChecker.canConvert("021 May!20161"));
	}
	@Test
	public void dateCustomizeValidInput() {
		assertTrue(((String)newsChecker.dateCustomize("Fri May 13 10:24:56 EEST 2016")).equals("13 May 2016") );
		assertTrue(((String)newsChecker.dateCustomize("Tue Mar 22 14:15:00 EET 2016")).equals("22 Mar 2016") );
	}
	@Test
	public void titleControl(){ //rss linkinde böyle bir title varmıdır fonksiyonun kontrolu
		String title=null;
		for (FeedMessage message : parserOfRss.feedParser(rssLinksAL.get(0))) {  //item to item reading
			title=message.getTitle(); //to get sample title
			break;
		}
		assertTrue(newsChecker.containNewsTitle(title, rssLinksAL.get(0))); //can this function find a title
	}
	
	@Test
	public void updateDatabaseControl(){    
		MongoDb mongoDbHelper = new MongoDb();
		ArrayList<String> dateWordFreq=mongoDbHelper.fetchWDocument();
		String date = dateWordFreq.get(0);
		String word = dateWordFreq.get(1);
		int frequency = Integer.parseInt(dateWordFreq.get(2));
		FeedMessage message = new FeedMessage();
		message.setPubDate(date);
		date=newsChecker.dateCustomize(message.getpubDate());
		newsChecker.addDatabase(date, word, 2);
		dateWordFreq = mongoDbHelper.fetchWDocument();
		String dateNew = dateWordFreq.get(0);
		String wordNew = dateWordFreq.get(1);
		int frequencyNew = Integer.parseInt(dateWordFreq.get(2));
		if (dateNew.equals(date) || word.equals(wordNew)) {
			assertEquals(frequencyNew, frequency+2);
		}else 
			fail("Can't get the same data which has same _id");
		mongoDbHelper.update(date, word, -2);
		}
	@AfterClass
	public static void stopJetty() throws Exception{
		server.stop();
	}
}
