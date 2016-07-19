package com.galaksiya.newsobserver.master;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.galaksiya.newsobserver.database.DatabaseFactory;

public class Inititator {

	private static final int SERVER_PORT = 8112;
	private Scanner sc;
	private Server jettyServer;

	private static final Logger LOG = Logger.getLogger(Inititator.class);

	/**
	 * It starts a web server(port 8112).
	 * 
	 * @throws Exception
	 */
	public boolean startWebserver()  {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		jettyServer = new Server(SERVER_PORT);
		jettyServer.setHandler(context);
		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		// Tells the Jersey Servlet which REST service/class to load.
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
				WebsiteContentCreator.class.getCanonicalName());
		try {
			jettyServer.start();
		} catch (Exception e) {
			LOG.error("JettyServer can not be started.",e);
			return false;
		}
		LOG.info("Jetty has been started.");
		return true;
	}

	/**
	 * It initiate the app.If it doesn't have args it asks for that.After the
	 * handle processings,menu function is called.
	 * 
	 * @param args
	 *            File path which will be read for URLs
	 */
	public boolean initiateApp(String databaseType, String filePath) {
		sc = new Scanner(System.in);

		if (filePath == null) {
			System.out.println("Give a path like:  /home/francium/new.txt");
			filePath = sc.nextLine();
		}
		DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
		databaseFactory.setDatabaseType(databaseType);
		FileParser fileParser = new FileParser(filePath);
		ScheduledRunner intervalFetcher = new ScheduledRunner();
		intervalFetcher.start(fileParser.getRssLinksAL());
		return startWebserver();
	}
}
