package com.galaksiya.newsObserver.master;

import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.galaksiya.newsObserver.database.DatabaseFactory;

public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);

	public static String _DatabaseType;


	private static final String PROPERTY_NAME = "databaseType";


	/**
	 * It is the main of a program..
	 * 
	 * @param args
	 *            File path which will be read for URLs
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String databaseType = "derby";// default
		CommandLineParser parser = new DefaultParser();
		Inititate initiate = new Inititate();
		// create Options object
		Options options = new Options();

		// add to option
		options.addOption("p", "rsspath", true, "display current time");
		CommandLine cmd = parser.parse(options, args);
		String filePath = null;
		if (cmd.hasOption("p")) {
			filePath = cmd.getOptionValue("p");
		} else {
			LOG.debug("Defolut database format.[derby]");
		}
		if(System.getProperty(PROPERTY_NAME) != null){
		if (  System.getProperty(PROPERTY_NAME).equalsIgnoreCase("mongo")) {
			databaseType = "mongo";
		} }
		
		if (initiate.initiateApp(databaseType, filePath)) {
			initiate.startWebserver();
		}

	}

	

	
}