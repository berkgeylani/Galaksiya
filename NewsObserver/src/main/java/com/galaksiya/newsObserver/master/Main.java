package com.galaksiya.newsObserver.master;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.galaksiya.newsObserver.database.MongoDb;

public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);

	public static void main(String[] args) throws IllegalArgumentException, IOException {
		String filePath;
		if (args == null || args.length == 0) {
			Scanner sc = new Scanner(System.in);
			System.out.println("Give a path like:  /home/francium/new.txt");
			filePath = sc.nextLine();
		} else
			filePath = args[0]; // jardan okumuyır şu anda dikkat et
		MongoDb mongoHelper = new MongoDb();
		mongoHelper.delete();
//		MainProcess mainprocessor = new MainProcess();
		FileParser fileParser = new FileParser(filePath); // process tx file and
															// return ArrayList
															// which contains
															// URLs

//		Hashtable<String, String> lastNews = mainprocessor.getResult(fileParser.getRssLinksAL());
//		 with hashtable rssLinks-Their last newsx
//		 mainprocessor.postToDatabase();// insert for all the news in all rss
		IntervalFetcher intervalFetcher = new IntervalFetcher();
		intervalFetcher.intervaller(fileParser.getRssLinksAL());// check
		
		menu();
	}

	private static void menu() {
		String dateChoosed = null;
		int flagMenu = 0;
		MongoDb mongoHelper = new MongoDb();
		try (Scanner sc = new Scanner(System.in)) {
			do {
				System.out.println("Insert a number for; [0 : Exit]");
				System.out.println("(Press 1)-  10 most used word from a date which will be chosen from you");
				System.out.println(
						"(Press 2)-  Sort by frequency from a date which will be chosen from you (All Worlds)");
				System.out.println("(Press 3)-  Sort all date sorted(freq)");

				System.out.println();

				try {
					
					flagMenu = sc.nextInt();// when read it's always 48 more and
											// then...(WILL SEARCH)
					dateChoosed = sc.nextLine();// eating the line for reading
												// error
					switch (flagMenu) {
					case 0:
						System.out.println(" --Wait for Exit--");
						break;
					case 1:
						System.out.println("Insert a date of day like :  17 Mar 2016\n");
						dateChoosed = sc.nextLine();
						System.out.println("10 most used word from " + dateChoosed);
						mongoHelper.fetch(dateChoosed,10);// from a day coming
														// limited our is top 10
														// and sorted(frequency)
						break;
					case 2:
						System.out.println("Insert a date of day like :  17 Mar 2016\n");
						dateChoosed = sc.nextLine();
						System.out.println("Sorted By Frequency from " + dateChoosed);
						mongoHelper.fetch(dateChoosed);// from a
															// date,printing
															// all,our document
															// is top 10 and
															// sorted(frequency)
						break;
					case 3:
						System.out.println("Wait For Print... ");
						mongoHelper.fetch();// from a date,printing all,our
											// document is top 10 and
											// sorted(frequency)
						break;
					default:
						System.out.println("Nice try but you need to insert that has already had functionality");
						break;
					}
				} catch (NoSuchElementException e) {
					LOG.error("Menu String input problem", e);
				}
			} while (flagMenu != 0);
		}
		LOG.info("Menu Hazır");
	}
}