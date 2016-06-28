package com.galaksiya.newsObserver.master;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.galaksiya.newsObserver.database.MongoDb;


public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);
	
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
        jettyServer.setStopTimeout(1_000_000);
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
		sc = new Scanner(System.in);
		String filePath;
		if (args == null || args.length == 0) {
			System.out.println("Give a path like:  /home/francium/new.txt");
			filePath = sc.nextLine();
		} else
			filePath = args[0]; 
		MongoDb mongoHelper = new MongoDb();
		mongoHelper.delete();
		FileParser fileParser = new FileParser(filePath);
		IntervalFetcher intervalFetcher = new IntervalFetcher();
		intervalFetcher.intervaller(fileParser.getRssLinksAL());
		return true;
	}
}