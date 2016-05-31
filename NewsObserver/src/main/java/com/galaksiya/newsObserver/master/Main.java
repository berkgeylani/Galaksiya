package com.galaksiya.newsObserver.master;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.galaksiya.newsObserver.database.MongoDb;

public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);

	private Scanner sc;

 /**
  * It is the main of a program.
  * @param args File path which will be read for URLs
  * @throws IllegalArgumentException
  * @throws IOException
  */
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		new Main().initiateApp(args);
	}
	/**
	 * It initiate the app.If it doesn't have args it asks for that.After the handle processings,menu function is called.
	 * @param args File path which will be read for URLs
	 */
	public void initiateApp(String[] args) {
		sc = new Scanner(System.in);
		String filePath;
		if (args == null || args.length == 0) {
			System.out.println("Give a path like:  /home/francium/new.txt");
			filePath = sc.nextLine();
		} else
			filePath = args[0]; // jardan okumuyır şu anda dikkat et
		MongoDb mongoHelper = new MongoDb();
		mongoHelper.delete();
		FileParser fileParser = new FileParser(filePath);
		IntervalFetcher intervalFetcher = new IntervalFetcher();
		intervalFetcher.intervaller(fileParser.getRssLinksAL());

		menu();
	}
	/**
	 * This function is gui.And the user can do whatever he wants.
	 */
	private void menu() {
		String dateChoosed = null;
		int flagMenu = 0;
		MongoDb mongoHelper = new MongoDb();
		do {
			System.out.println("Insert a number for; [0 : Exit]");
			System.out.println("(Press 1)-  10 most used word from a date which will be chosen from you");
			System.out.println("(Press 2)-  Sort by frequency from a date which will be chosen from you (All Worlds)");
			System.out.println("(Press 3)-  Sort all date sorted(freq)");

			System.out.println();

			try {
				do{
				flagMenu = sc.nextInt();
				dateChoosed = sc.nextLine();
				}while(flagMenu>=0 && flagMenu<=3 && dateChoosed!= null);
				switch (flagMenu) {
				case 0:
					System.out.println(" --Wait for Exit--");
					break;
				case 1:
					System.out.println("Insert a date of day like :  17 Mar 2016\n");
					dateChoosed = sc.nextLine();
					System.out.println("10 most used word from " + dateChoosed);
					mongoHelper.fetch(dateChoosed, 10);
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
		LOG.info("Menu Hazır");
	}
}