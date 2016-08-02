package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class ScheduledRunner {
	private static final Logger LOG = Logger.getLogger("com.newsobserver.admin");
	private ScheduledExecutorService executor;
	private BlockingQueue<Feed> sharedFeedQueue;
	private BlockingQueue<URL> sharedURLQueue;

	public ScheduledRunner() {
		sharedFeedQueue = new LinkedBlockingQueue<>();
		sharedURLQueue = new LinkedBlockingQueue<>();
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
		executor = Executors.newScheduledThreadPool(3);
		
		
		// Runnable periodicTask = () -> { TODO java 8 için bunu dene.
		// newsChecker.updateActualNews();
		// LOG.info("Checked all the rss links.\n\n\n");
		// };

		Runnable periodicTask = new Runnable() {
			public void run() {
				sharedURLQueue.addAll(fileParser.getRssLinks());
				LOG.info("Thread başlatıldı.");
				new Thread(new ProducerOfRssFeed(sharedURLQueue, sharedFeedQueue), "ProduceFeedThread").start();
				new Thread(new NewsChecker(sharedURLQueue, sharedFeedQueue), "ProcessFeedThread").start();
			}
		};

		executor.scheduleAtFixedRate(periodicTask, 0, 5, TimeUnit.MINUTES);
	}
}