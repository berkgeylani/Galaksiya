package com.galaksiya.newsObserver.master;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.galaksiya.newsObserver.database.Database;
import com.galaksiya.newsObserver.database.DatabaseFactory;


public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static String _DatabaseType ;
	 
	private static final int SERVER_PORT = 8112;

	private Scanner sc;

	private Server jettyServer;

 /**
  * It is the main of a program..
  * @param args File path which will be read for URLs
 * @throws Exception 
  */
	public static void main(String[] args) throws Exception {
		if(new Main().initiateApp(args))
			new Main().startWebserver();
	}
	/**
	 * It starts a web server(port 8112).
	 * @throws Exception
	 */
	public void startWebserver() throws Exception {
		
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
           WebsiteContentCreator.class.getCanonicalName());
            jettyServer.start();
            LOG.info("Jetty has been started.");
	}
	/**
	 * It initiate the app.If it doesn't have args it asks for that.After the handle processings,menu function is called.
	 * @param args File path which will be read for URLs
	 */
	public boolean initiateApp(String[] args){
		DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
		sc = new Scanner(System.in);
		String filePath=null;
		if (args == null || args.length == 0) {
			System.out.println("Give a path like:  /home/francium/new.txt");
			filePath = sc.nextLine();
			System.out.println("Give a database type like:  [mongo|derby]");
			databaseFactory.setDatabaseType(selectDatabase(sc.nextLine()));
		} else if (args.length==1) {
			LOG.error("Need to insert two arg.");
			System.exit(0);
		}if(args.length==2){
			filePath=args[0];
			databaseFactory.setDatabaseType(selectDatabase(args[2]));
		}
		Database dbHelper = databaseFactory.getDatabase();
		dbHelper.delete();
		Database dbNewsHelper = databaseFactory.getDatabase("news");  
		dbNewsHelper.delete();
		FileParser fileParser = new FileParser(filePath);
		IntervalFetcher intervalFetcher = new IntervalFetcher();
		intervalFetcher.intervaller(fileParser.getRssLinksAL());
		return true;
	}
	private String selectDatabase(String databaseType) {
		if (databaseType.equalsIgnoreCase("mongo")) {
			return "mongo";
		} else if(databaseType.equalsIgnoreCase("derby")){
			return "derby";
		}
		else {
			LOG.fatal("Wrong database type.Please execute this app again.");
			System.exit(0);
		}
		return null;
	}
}