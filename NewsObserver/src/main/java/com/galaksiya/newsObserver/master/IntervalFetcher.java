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

	/**
	 * This function does their tasks repeatly in 5 min and the repeated task is
	 * check for the actual news.
	 * 
	 * @param RssLinksAL
	 *            It is a arraylist which occurs from urls of rss's
	 */
	public void intervaller(ArrayList<URL> RssLinksAL) {
		NewsChecker newsChecker = new NewsChecker();
		// lastNewsStatic = lastNews;
		executor = Executors.newSingleThreadScheduledExecutor();

		Runnable periodicTask = new Runnable() {
			public void run() {
				// Every 5 min this which controlling a
				newsChecker.updateActualNews(RssLinksAL);
				LOG.info("Checked all the rss links.");
			}
		};
		executor.scheduleAtFixedRate(periodicTask, 0, 6, TimeUnit.MINUTES);
	}

	/**
	 * It closes the intervaller.
	 */
	public void intervalCloser() {
		executor.shutdownNow();
		LOG.debug("Intervaller closed.");
	}
}