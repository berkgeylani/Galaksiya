package com.galaksiya.newsobserver.master;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
/**
 * Main function and arguman reader which is comm'in from terminal.
 * @author francium
 *
 */
public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);


	private static final String PROPERTY_NAME = "databaseType";
	public static final long START_TIME= System.nanoTime();

	private static final String DB_PROPERTY = System.getProperty(PROPERTY_NAME);

	/**
	 * It is the main of a program..
	 *  
	 * @param args
	 *            File path which will be read for URLs
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LOG.info("Program başlangıcı\t"+(System.nanoTime()-START_TIME)/ 1000000000.0);
		String databaseType = "mongo";// default
		CommandLineParser parser = new DefaultParser();
		Inititator initiate = new Inititator();
		// create Options object
		Options options = new Options();
		// add to option
		options.addOption("p", "rsspath", true, "display current time");
		CommandLine cmd = parser.parse(options, args);
		String filePath = null;
		if (cmd.hasOption("p")) {
			filePath = cmd.getOptionValue("p");
		} else {
			LOG.debug("Default database format.["+databaseType+"]");
		}
		if (DB_PROPERTY != null && DB_PROPERTY.equalsIgnoreCase("mongo")) {
				databaseType = "mongo";
		}
		if (initiate.initiateApp(databaseType, filePath)) {
			LOG.info("Program started.");
		}else{
			LOG.error("Program was closed.");
			System.exit(0);
		}
		
	}

}