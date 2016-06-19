package com.galaksiya.newsObserver.master;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsObserver.database.MongoDb;

public class WebsiteContentCreatorTest {
	private Server jettyServer;
	
	private static final int SERVER_PORT = 8112;
	
	private WebsiteContentCreator contentCreator = new WebsiteContentCreator("test");
	
	private ArrayList<Document> dataAl;
    
	@Test
	public void createContextNullInput(){
		assertEquals(null,contentCreator.createContext(null) );
	}
	@Test
	public void createContextZeroSizeInput(){
		assertEquals(null,contentCreator.createContext(new ArrayList<Document>()) );
	}
	@Before
	public void Before(){
		dataAl= new ArrayList<Document>();
	}
	@Test
	public void canCreate(){
		NewsChecker newsChecker = new NewsChecker();
		Document document = new Document();
		document.append("date", newsChecker.dateCustomize("Wed Jun 01 00:00:00 EEST 2016").toString());
		document.append("word", "Atalay");
		document.append("frequency", "6");
		dataAl= new ArrayList<Document>();
		dataAl.add(document);
		MongoDb mongoDbHelper = new MongoDb();
		mongoDbHelper.save(document.get("date").toString(), document.get("word").toString(), Integer.parseInt(document.get("frequency").toString()));
		//şimdi database kaydettik
		//şimdi elimizdkeiyle veriyle bir website oluşturalım
		
	}
	
	@Test
	public void createContextZeroCanCreate(){
		NewsChecker newsChecker = new NewsChecker();
		Document document = new Document();
		document.append("date", "Wed Jun 01 00:00:00 EEST 2016");
		document.append("word", "Atalay");
		document.append("frequency", "6");
		dataAl.add(document);
		String content="<html>"
				+"<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Data</title></head>"
				+"<body>"
				+"<h4>Data</h4>"
				+"<ul>"
					+"<li>"+newsChecker.dateCustomize(document.get("date").toString())+"</li>"
					+"<li>"+document.get("word").toString()+"</li>"
					+"<li>"+document.get("frequency").toString()+"</li>"
					+"</ul>"
				+"</body>"
				+"</html>";
		assertEquals(content,contentCreator.createContext(dataAl) );
	}
	@Test
	public void topLimitForDayBadInput(){
	  	assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.topLimitForADay(null,"Jun","2016",  1).getStatus());//bad request
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.topLimitForADay("01",null,"2016", 1).getStatus());//bad request
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.topLimitForADay("01","Jun",null,1).getStatus());//bad request
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.topLimitForADay("01","Jun","2016",-1).getStatus());//bad request
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),contentCreator.topLimitForADay("01","Jun","2016",1).getStatus());//status okey

	}
	@Test
	public void forDayBadInput(){
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.forADay(null,"Jun","2016").getStatus());//bad request
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.forADay("01",null,"2016").getStatus());//bad request
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),contentCreator.forADay("01","Jun",null).getStatus());//bad request
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),contentCreator.forADay("01","Jun","2016").getStatus());//status okey
	}
	@Test
	public void topLimitForDayNoContent(){
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),contentCreator.topLimitForADay("31", "May", "1961", 5).getStatus());//no content
	}
	
	@Test
	public void forDayNoContent(){
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),contentCreator.forADay("31", "May", "1961").getStatus());//no content
	}
	//sayfa oluştur testte
	//jsoup kullanılarak sayfa içeriğini çek ve kontrol et
	
	private void startWebServer() throws Exception{
			
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	 
	        jettyServer = new Server(SERVER_PORT);
	        jettyServer.setHandler(context);
	  
	        ServletHolder jerseyServlet = context.addServlet(
	             org.glassfish.jersey.servlet.ServletContainer.class, "/*");
	        jerseyServlet.setInitOrder(0);
	        // Tells the Jersey Servlet which REST service/class to load.
	        jerseyServlet.setInitParameter(
	           "jersey.config.server.provider.classnames",
	           new WebsiteContentCreator("test").getClass().getCanonicalName());
	            jettyServer.start();
		
	}
	@Test
	public void sortedAllContentTest() throws Exception{
		startWebServer();
		//arada database bişeyler ekle
		MongoDb mongoDb = new MongoDb("test");
		mongoDb.save("01 Jun 2000", "Galaksiya", 5);
		contentCreator.sortedAll();
	}
}
