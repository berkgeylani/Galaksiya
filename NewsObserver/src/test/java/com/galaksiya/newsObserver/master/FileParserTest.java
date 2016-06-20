package com.galaksiya.newsObserver.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.galaksiya.newsObserver.master.testutil.CreateRssJetty;

public class FileParserTest {
	/*
	 * emptyPathWay:checks behaviour when inputs are null or empty string
	 * wrongPathWay: checks pathway is true but is there an any txt file which
	 * we need
	 */
	private String file = "fileParserTest.txt";
	private FileParser testFileParser = new FileParser(file);
	private static final int SERVER_PORT = 8119;

	private ArrayList<String> rssLinksAL = new ArrayList<String>();
	private static Server server = new Server(SERVER_PORT);//static yaptık çünkü classın initialize'dan edilmeden önce çalıştırılması gerekiyor.
	
	@BeforeClass
	public static void startJetty() throws Exception{
        server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class).setHttpCompliance(HttpCompliance.LEGACY);
        server.setHandler(new CreateRssJetty());
        server.setStopAtShutdown(true);
        server.start();
	}
	@Before
	public void setup() throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file))) {
			rssLinksAL.add("http://localhost:"+SERVER_PORT+"/");
			for (String rssLink : rssLinksAL) {
				writer.write(rssLink);// We wrote a txt file
			}
		}
	}

	@Test
	public void emptyPathWay() {
		assertFalse(testFileParser.readerOfFile(""));
		assertFalse(testFileParser.readerOfFile(null));
	}

	@Test
	public void wrongPathWay() {

		assertFalse(testFileParser.readerOfFile(System.getProperty("user.dir")));// what
																					// will
																					// function
																					// do
																					// in
																					// worng
																					// path?
		assertFalse(!testFileParser.readerOfFile(file)); // givin truepath and
															// response should
															// be true
		/*
		 * examples path("c:/test"); //returns true path("c:/te:t"); //returns
		 * false path("c:/te?t"); //returns false path("c/te*t"); //returns
		 * false path("good.txt"); //returns true path("not|good.txt");
		 * //returns false path("not:good.txt"); //returns false
		 */
	}

	@Test
	public void canReadTxt() { // check reading line size with arraylist size
		testFileParser.readerOfFile(file);
		assertEquals(rssLinksAL.size(), testFileParser.getRssLinksAL().size());
	}

	@Test
	public void givenArrayListContainsURL() {// can it translate all the lines
												// to url
		testFileParser.readerOfFile(file);
		try {
			for (URL URLs : testFileParser.getRssLinksAL()) // This is just
															// iterate on
															// rssLinksAL and
															// check are they
															// all URL?
			{
				new URL(URLs.toString());
			}
		} catch (MalformedURLException exception) {
			Assert.fail("FileParrser.readerOFFile can't convert string to URL");
		}
	}
	@After
	public void deleteSetup() {
		try {
			Files.deleteIfExists(Paths.get(file));
		} catch (IOException x) {
			// File permission problems are caught here.
			System.err.println(x);
		}
	}
	@AfterClass
	public static void stopJetty() throws Exception{
		server.stop();
	}
}