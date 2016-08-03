package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class ScheduledRunner {
	private static final Logger LOG = Logger.getLogger(ScheculedRunner.class);
	private ScheduledExecutorService executor;
	private BlockingQueue<Feed> sharedFeedQueue;
	private BlockingQueue<URL> sharedURLQueue;

	public ScheduledRunner() {
		sharedFeedQueue = new LinkedBlockingQueue<>();
		sharedURLQueue = new LinkedBlockingQueue<>();
		executor = Executors.newScheduledThreadPool(11);
	}

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
	 * @param RssLinksBlockingQueue
	 *            It is a arraylist which occurs from urls of rss's
	 */
	public void start(String filePath) {
		FileParser fileParser = new FileParser(filePath);
		Runnable[] rssFetcherRunnableArray = new Runnable[11];
		Runnable rssFetcherInitializerRunnable = () -> {
			LOG.info("Rss fetching started.");
			sharedURLQueue.addAll(fileParser.getRssLinks());
		};
		rssFetcherRunnableArray[0] = rssFetcherInitializerRunnable;

		for (int i = 1; i < rssFetcherRunnableArray.length; i++) {
			rssFetcherRunnableArray[i] = () -> {
				RssFetcher rssFetcher = new RssFetcher(sharedURLQueue, sharedFeedQueue);
				rssFetcher.fetch();
			};
		}
		for (int i = 0; i < 10; i++) {
			LOG.info("Consumer thread-"+i+" has been started.");
			new Thread(new NewsChecker(sharedFeedQueue)).start();
		}
		for (int i = 0; i <   rssFetcherRunnableArray.length; i++) { //Producer count-1
			executor.scheduleAtFixedRate(rssFetcherRunnableArray[i], 0, 5, TimeUnit.MINUTES);
			if (i == 0) {
				try {
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					LOG.error("Problem while sleeping",e);
				}
			}
		}
	}
}