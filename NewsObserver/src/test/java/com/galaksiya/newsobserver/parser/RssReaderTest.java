package com.galaksiya.newsobserver.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.galaksiya.newsobserver.parser.FeedMessage;
import com.galaksiya.newsobserver.parser.RssReader;
import com.galaksiya.newsobserver.parser.testutil.CreateNoNewRssWebsite;
import com.galaksiya.newsobserver.parser.testutil.CreateNonRssWebsite;
import com.galaksiya.newsobserver.parser.testutil.CreateRssWebsite;

public class RssReaderTest {

	private static Server server;

	private static final int SERVER_PORT = 4000;

	@BeforeClass
	public static void startJetty()  {
		server = new Server(SERVER_PORT);

		ContextHandler context = new ContextHandler("/nonew");
		context.setContextPath("/nonew");
		context.setHandler(new CreateNoNewRssWebsite());

		ContextHandler context1 = new ContextHandler("/nonrss");
		context1.setHandler(new CreateNonRssWebsite());

		ContextHandler context2 = new ContextHandler("/rss");
		context2.setHandler(new CreateRssWebsite());
		
		ContextHandler context3 = new ContextHandler("/rssMalformedItem");
		context2.setHandler(new CreateRssWebsite());

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { context, context1, context2 , context3});

		server.setHandler(contexts);

		server.setStopAtShutdown(true);
		try {
			server.start();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	@AfterClass
	public static void stopJetty() {
		try {
			server.stop();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	private RssReader rssRead= new RssReader();
	@Test
	public void canReadrssReader() throws MalformedURLException, ParseException{ // true rss,link,new 4
		ArrayList<FeedMessage> itemsAL = rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/rss"));
		FeedMessage message = new FeedMessage();
		message.setTitle("New York Times Ortadoğu’nun sınırlarını yeniden çizdi; Türkiye’yi böldü");
		message.setDescription(
				"New York Times gazetesi, Osmanlı topraklarının paylaşılmasını öngören ve tüm taraflarla imzalanan Sykes-Picot Anlaşmasının 100. yıldönümünde arşivinden yeni bir harita çıkardı. Haritalar ise İngiltere ve Fransanın hazırladığı Sykes-Picotun alternatifleri. ORTADOĞU HARİTASI BU ŞEKİLDE ÇİZİLSEYDİ Haberde dönemin ABD Başkanı Woodrow Wilson tarafından hazırlatılan haritayla birlikte,1920lerde sınırlar bu şekilde çizilseydi Ortadoğu kurtarılabilir miydi? sorusu da yer alıyor. []");
		message.setPubDate("Tue Jun 21 13:16:16 EEST 2016"
				+ "");
		assertTrue(areEqual(message, itemsAL.get(0)));
	}
	@Test
	public void canReadMalformedItem() throws MalformedURLException, ParseException{ // true rss,link,new 4
		ArrayList<FeedMessage> itemsAL = rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/rssMalformedItem"));
		FeedMessage message = new FeedMessage();
		assertTrue(itemsAL.isEmpty());
	}
	@Test
	public void rssReaderNoNew() throws MalformedURLException{ //true link,rss no new 3
		assertTrue(rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/nonew")).isEmpty()) ; 
	}
	@Test
	public void rssReaderNonRssInput() throws MalformedURLException{ //true link,false rss 2
		assertTrue(rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/nonrss")).isEmpty()); 
	}
	//empty URL - not neccessary
	//wrong URL - done 1
	//true url but not rss -done 2
	//true url rss but no new -done 3 
	//can create messages -done 4
	@Test
	public void rssReadernullInput(){ //nullInput 1 
		assertEquals(null,rssRead.parseFeed(null)); 
	}
	@Test
	public void rssReaderWrongURL() throws MalformedURLException{ //wrong url
		assertTrue(rssRead.parseFeed(new URL("http:/localhost:"+SERVER_PORT+"/rss")).isEmpty()); 
	}
	private boolean areEqual(FeedMessage message,FeedMessage message2) throws ParseException{
		boolean isDescriptionEqual = message.getDescription().equals(message2.getDescription());
		boolean isTitleEqual = message.getTitle().equals(message2.getTitle());
		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
		Date date1,date2;
		try {//Tue Jun 21 13:16:16 EEST 2016
			date1=formatter.parse(message.getpubDate());
			date2=formatter.parse(message2.getpubDate());
		} catch (ParseException e) {
			throw new ParseException(message.getpubDate()+"-1----2-"+message2.getpubDate() +"couldn't parse one of them.", 0);
		}
		boolean isPubDateEqual =date1.equals(date2);//Tue, 21 Jun 2016 13:16:16----Tue Jun 21 13:16:16 EEST 2016
		return isDescriptionEqual && isTitleEqual && isPubDateEqual;
	}
}