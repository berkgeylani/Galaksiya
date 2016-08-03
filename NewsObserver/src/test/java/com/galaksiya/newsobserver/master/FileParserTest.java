package com.galaksiya.newsobserver.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.galaksiya.newsobserver.database.MongoDb;
import com.galaksiya.newsobserver.database.MongoDbTest;
import com.galaksiya.newsobserver.master.FileParser;
import com.galaksiya.newsobserver.master.testutil.CreateRssJetty;

public class FileParserTest {
	private static Server server;
	private static final int SERVER_PORT = 8119;
	private static final Logger LOG = Logger.getLogger(FileParserTest.class);

	@AfterClass
	public static void shutDown() {
		MongoDb mongoDb = new MongoDb("test");
		mongoDb.delete();
		MongoDb mongoDbNews = new MongoDb("newsTest");
		mongoDbNews.delete();
	}

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

	/*
	 * emptyPathWay:checks behaviour when inputs are null or empty string
	 * wrongPathWay: checks pathway is true but is there an any txt file which
	 * we need
	 */
	private String file = "fileParserTest.txt";

	private ArrayList<String> rssLinksAL = new ArrayList<>();

	private FileParser testFileParser = new FileParser(file);

	@Test
	public void canReadTxt() { // check reading line size with arraylist size
		testFileParser.readFile(file);
		assertEquals(rssLinksAL.size(), testFileParser.getRssLinks().size());
	}

	@After
	public void deleteSetup() {
		try {
			Files.deleteIfExists(Paths.get(file));
		} catch (IOException x) {
			LOG.error("Couldn't delete the file. -->",x);
		}
	}

	@Test
	public void emptyPathWay() {
		assertFalse(testFileParser.readFile(""));
		assertFalse(testFileParser.readFile(null));
	}

	@Test
	public void invalidRssLink() throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file))) {
			rssLinksAL.add("\n|@~");
			for (String rssLink : rssLinksAL) {
				writer.write(rssLink);// We wrote a txt file
			}
		}
		assertFalse(testFileParser.readFile(file));
	}

	@Before
	public void setup() throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file))) {
			rssLinksAL.add("http://localhost:" + SERVER_PORT + "/");
			for (String rssLink : rssLinksAL) {
				writer.write(rssLink);// We wrote a txt file
			}
		}
	}

	@Test
	public void wrongPathWay() {

		assertFalse(testFileParser.readFile(System.getProperty("user.dir")));
		assertFalse(!testFileParser.readFile(file)); // givin truepath and
															// response should
															// be true
	}
}