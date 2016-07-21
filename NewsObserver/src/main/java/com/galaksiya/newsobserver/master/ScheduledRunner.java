package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class ScheduledRunner {
	private static final Logger LOG = Logger.getLogger(ScheduledRunner.class);
	private ScheduledExecutorService executor;

	/**
	 * It closes the intervaller.
	 */
	public void shutdown() {
		executor.shutdownNow();
		LOG.debug("Intervaller closed.");
	}

	/**
	 * This function does their tasks repeatly in 5 min and the repeated task is
	 * check for the actual news.
	 * 
	 * @param RssLinksAL
	 *            It is a arraylist which occurs from urls of rss's
	 */
	public void start(ArrayList<URL> RssLinksAL) {
		NewsChecker newsChecker = new NewsChecker(RssLinksAL);
		executor = Executors.newSingleThreadScheduledExecutor();

		Runnable periodicTask = new Runnable() {
			public void run() {
				newsChecker.updateActualNews();
				LOG.info("Checked all the rss links."); 
			}
		};
		executor.scheduleAtFixedRate(periodicTask, 0, 15, TimeUnit.MINUTES);
	}
}