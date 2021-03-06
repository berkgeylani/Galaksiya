package com.galaksiya.newsobserver.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.galaksiya.newsobserver.database.Database;
import com.galaksiya.newsobserver.database.MongoDb;

public class WebsiteContentCreatorTest {
	@AfterClass
	public static void shutDown() {
		MongoDb mongoDb = new MongoDb("test");
		mongoDb.delete();
	}

	private Database mongoDb = new MongoDb("test");
	
	private WebsiteContentCreator contentCreator = new WebsiteContentCreator(mongoDb);

	private ArrayList<Document> dataAl;

	@Before
	public void Before() {
		Database mongoDb = new MongoDb("test");
		mongoDb.delete();
		dataAl = new ArrayList<>();
	}

	@Test
	public void canCreate() {
		DateUtils dateUtils = new DateUtils();
		Document document = new Document();
		document.append("date", dateUtils.dateCustomize("Wed Jun 01 00:00:00 EEST 2016").toString());
		document.append("word", "Atalay");
		document.append("frequency", "6");
		dataAl = new ArrayList<>();
		dataAl.add(document);
		Database mongoDbHelper = new MongoDb("test");
		mongoDbHelper.save(document.get("date").toString(), document.get("word").toString(),
				Integer.parseInt(document.get("frequency").toString()));
		// şimdi database kaydettik
		// şimdi elimizdkeiyle veriyle bir website oluşturalım
	}

	@Test
	public void createContextNullInput() {
		assertEquals(null, contentCreator.createContext(null,"newsTest"));
	}

	@Test
	public void createContextZeroCanCreate() {
		DateUtils dateUtils = new DateUtils();
		Document document = new Document();
		document.append("date", "Wed Jun 01 00:00:00 EEST 2016").append("word", "Atalay").append("frequency", "6");
		dataAl.add(document);
		String content = "<html>"
				+ "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Data-0</title></head>"
				+ "<body>" + "<h4>Data</h4>" + "<ul>" + "<li>"
				+ dateUtils.dateCustomize(document.get("date").toString()) + "</li>" + "<li>"
				+ document.get("word").toString() + "</li>" + "<li>" + document.get("frequency").toString() + "</li>"
				+ "</ul>" + "</body>" + "</html>";
		assertEquals(content, contentCreator.createContext(dataAl,"newsTest"));
	}

	@Test
	public void createContextZeroSizeInput() {
		assertEquals(null, contentCreator.createContext(new ArrayList<Document>(),"newsTest"));
	}

	@Test
	public void forADayBadInputDay() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.forADay(null, "Jun", "01").getStatus());
	}

	@Test
	public void forADayBadInputMonth() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.forADay("2016", null, "01").getStatus());
	}

	@Test
	public void forADayBadInputYear() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.forADay("2016", "Jun", null).getStatus());
	}

//	@Test
//	public void ForADayConnectWithoutMongoDbConnection() {
//		Database mockedMongo = mock(MongoDb.class);
//		when(mockedMongo.fetch(anyString())).thenReturn(null);
//		WebsiteContentCreator contentCreatorWMockedMongo = new WebsiteContentCreator(mockedMongo);
//		assertEquals(Response.status(Status.INTERNAL_SERVER_ERROR).build().getStatus(),
//				contentCreatorWMockedMongo.forADay("01", "Jun", "2000").getStatus());
//	}

	@Test
	public void forADayContentTest() {
		mongoDb.save("01 Jun 2000", "Galaksiya", 5);
		org.jsoup.nodes.Document doc = Jsoup.parse(contentCreator.forADay("2000", "Jun", "01").getEntity().toString());
		Elements ul = doc.select("ul");
		Elements li = ul.select("li"); // select all li from ul
		boolean isAllEquals = false;
		if (li.get(0).text().equals("01 Jun 2000") && li.get(1).text().equals("Galaksiya")
				&& li.get(2).text().equals("5")) {
			isAllEquals = true;
		}
		assertTrue(isAllEquals);
	}

	@Test
	public void forADayNoContent() {
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),
				contentCreator.forADay("2016", "Jun", "01").getStatus());// status
	}

	@Test
	public void forDayNoContent() {
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),
				contentCreator.forADay("1961", "May", "31").getStatus());// no
	}

//	@Test
//	public void sortedAllConnectWithoutMongoDbConnection()  {
//		Database mockedMongo = mock(MongoDb.class);
//		when(mockedMongo.fetch()).thenReturn(null);
//		WebsiteContentCreator contentCreatorWMockedMongo = new WebsiteContentCreator(mockedMongo);
//		assertEquals(Response.status(Status.INTERNAL_SERVER_ERROR).build().getStatus(),
//				contentCreatorWMockedMongo.sortedAll().getStatus());
//	}

	@Test
	public void sortedAllContentTest(){
		mongoDb.save("01 Jun 2000", "Galaksiya", 5);
		org.jsoup.nodes.Document doc = Jsoup.parse(contentCreator.sortedAll().getEntity().toString());
		Elements ul = doc.select("ul");
		Elements li = ul.select("li"); // select all li from ul
		boolean isAllEquals = false;
		if (li.get(0).text().equals("01 Jun 2000") && li.get(1).text().equals("Galaksiya")
				&& li.get(2).text().equals("5")) {
			isAllEquals = true;
		}
		assertTrue(isAllEquals);
	}

	@Test
	public void sortedAllNoContent() {
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),
				contentCreator.sortedAll().getStatus());// no
	}
	@Test
	public void topLimitForADayContentTest() {
		mongoDb.save("01 Jun 2000", "Galaksiya", 5);
		org.jsoup.nodes.Document doc = Jsoup
				.parse(contentCreator.topLimitForADay("2000", "Jun", "01", 1).getEntity().toString());
		Elements ul = doc.select("ul");
		Elements li = ul.select("li"); // select all li from ul
		boolean isAllEquals = false;
		if (li.get(0).text().equals("01 Jun 2000") && li.get(1).text().equals("Galaksiya")
				&& li.get(2).text().equals("5")) {
			isAllEquals = true;
		}
		assertTrue(isAllEquals);
	}

	@Test
	public void topLimitForDayBadInputDay() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.topLimitForADay(null, "Jun", "01", 1).getStatus());// bad
	}

	@Test
	public void topLimitForDayBadInputLimit() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.topLimitForADay("2016", "Jun", "01", -1).getStatus());
	}

	@Test
	public void topLimitForDayBadInputMonth() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.topLimitForADay("2016", null, "01", 1).getStatus());// bad
	}

	@Test
	public void topLimitForDayBadInputYear() {
		assertEquals(Response.status(Status.BAD_REQUEST).build().getStatus(),
				contentCreator.topLimitForADay("2016", "Jun", null, 1).getStatus());// bad
	}

//	@Test
//	public void TopLimitForDayConnectWithoutMongoDbConnection() {
//		Database mockedMongo = mock(MongoDb.class);
//		when(mockedMongo.fetch(anyString(), anyInt())).thenReturn(null);
//		WebsiteContentCreator contentCreatorWMockedMongo = new WebsiteContentCreator(mockedMongo);
//		assertEquals(Response.status(Status.INTERNAL_SERVER_ERROR).build().getStatus(),
//				contentCreatorWMockedMongo.topLimitForADay("01", "Jun", "2000", 5).getStatus());
//	}

	@Test
	public void topLimitForDayNoContent() {
		assertEquals(Response.status(Status.NO_CONTENT).build().getStatus(),
				contentCreator.topLimitForADay("1961", "May", "31", 5).getStatus());// no
	}
}
