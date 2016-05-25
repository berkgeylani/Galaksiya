package com.galaksiya.newsObserver.master;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class IntervalFetcher {
	private static final Logger LOG = Logger.getLogger(IntervalFetcher.class);
	ScheduledExecutorService executor;

	public void intervaller(ArrayList<URL> RssLinksAL) {
		NewsChecker newsChecker = new NewsChecker();
		//		lastNewsStatic = lastNews;
		executor = Executors.newSingleThreadScheduledExecutor();
		
		Runnable periodicTask = new Runnable() {
			public void run() {
				// Every 5 min this which controlling a 
				newsChecker.updateActualNews(RssLinksAL);
				LOG.info("Checked all the rss links.");
			}
		};
		executor.scheduleAtFixedRate(periodicTask, 0, 5, TimeUnit.MINUTES);
	}
	public void intervalCloser(){
		executor.shutdownNow();	
		LOG.debug("Intervaller closed.");
	}	
}