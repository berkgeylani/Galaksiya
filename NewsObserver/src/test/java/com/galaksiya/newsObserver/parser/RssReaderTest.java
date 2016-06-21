package com.galaksiya.newsObserver.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.galaksiya.newsObserver.parser.testutil.CreateNoNewRssWebsite;
import com.galaksiya.newsObserver.parser.testutil.CreateNonRssWebsite;
import com.galaksiya.newsObserver.parser.testutil.CreateRssWebsite;

public class RssReaderTest {

	private static Server server;
	
	private static final int SERVER_PORT = 4000;

	@BeforeClass
	public static void startJetty() throws Exception {
		server = new Server(SERVER_PORT);

		ContextHandler context = new ContextHandler("/nonew");
        context.setContextPath("/nonew");
        context.setHandler(new CreateNoNewRssWebsite());

        ContextHandler context1 = new ContextHandler("/nonrss");
        context1.setHandler(new CreateNonRssWebsite());

        ContextHandler context2 = new ContextHandler("/rss");
        context2.setHandler(new CreateRssWebsite());
        
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context, context1, context2 });

        server.setHandler(contexts);

		server.setStopAtShutdown(true);
		server.start();
	}

	@AfterClass
	public static void stopJetty() throws Exception {
		server.stop();
	}
	private RssReader rssRead= new RssReader();
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
		assertEquals(null,rssRead.parseFeed(new URL("http:/localhost:"+SERVER_PORT+"/rss"))); 
	}
	private boolean areEqual(FeedMessage message,FeedMessage message2){
		boolean isDescriptionEqual = message.getDescription().equals(message2.getDescription());
		boolean isTitleEqual = message.getTitle().equals(message2.getTitle());
		String message1pubdate=message.getpubDate().substring(5, 7)+message.getpubDate().substring(8, 11)+message.getpubDate().substring(12, 16);
		String message2pubdate=message2.getpubDate().substring(8, 10)+message2.getpubDate().substring(4, 7)+message2.getpubDate().substring(25, 29);
		boolean isPubDateEqual = message1pubdate.equals(message2pubdate);
		System.out.println(isPubDateEqual+"-------"+message1pubdate+"----------"+message2pubdate);
		return isDescriptionEqual && isTitleEqual && isPubDateEqual;
	}
	@Test
	public void canReadrssReader() throws MalformedURLException{ // true rss,link,new 4
		ArrayList<FeedMessage> itemsAL = rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/rss"));
		FeedMessage message = new FeedMessage();
		message.setTitle("New York Times Ortadoğu’nun sınırlarını yeniden çizdi; Türkiye’yi böldü");
		message.setDescription(
				"New York Times gazetesi, Osmanlı topraklarının paylaşılmasını öngören ve tüm taraflarla imzalanan Sykes-Picot Anlaşmasının 100. yıldönümünde arşivinden yeni bir harita çıkardı. Haritalar ise İngiltere ve Fransanın hazırladığı Sykes-Picotun alternatifleri. ORTADOĞU HARİTASI BU ŞEKİLDE ÇİZİLSEYDİ Haberde dönemin ABD Başkanı Woodrow Wilson tarafından hazırlatılan haritayla birlikte,1920lerde sınırlar bu şekilde çizilseydi Ortadoğu kurtarılabilir miydi? sorusu da yer alıyor. []");
		message.setPubDate("Mon, 16 May 2016 16:27:22"
				+ "");
		assertTrue(areEqual(message, itemsAL.get(0)));
	}
	@Test
	public void rssReaderNonRssInput() throws MalformedURLException{ //true link,false rss 2
		assertEquals(null,rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/nonrss"))); 
	}
	@Test
	public void rssReaderNoNew() throws MalformedURLException{ //true link,rss no new 3
		assertEquals(null,rssRead.parseFeed(new URL("http://localhost:"+SERVER_PORT+"/nonew"))); 
	}
	
}