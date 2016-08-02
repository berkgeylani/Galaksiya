package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.galaksiya.newsobserver.parser.RssReader;

public class RssFetcher implements Runnable {

	private static final Logger LOG = Logger.getLogger("com.newsobserver.admin");

	private BlockingQueue<URL> sharedURLQueue;
	private BlockingQueue<Feed> sharedFeedQueue;

	private RssReader reader = new RssReader();

	public RssFetcher(BlockingQueue<URL> sharedURLQueue, BlockingQueue<Feed> sharedFeedQueue) {
		this.sharedURLQueue = sharedURLQueue;
		this.sharedFeedQueue = sharedFeedQueue;
	}

	@Override
	public void run() {
		while (!sharedURLQueue.isEmpty()) {
			try {
				// önce al oku sonra feed object oalrak kuyruğa ekle
				URL url = sharedURLQueue.take();
				LOG.info("Producing feeds for this url :\t" + url);
				sharedFeedQueue.put(new Feed(url, reader.read(url)));
			} catch (InterruptedException e) {
				LOG.error(e);
			}
		}
	}
}
