package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.galaksiya.newsobserver.parser.RssReader;
/**
 * thi class is reading a url from sharedURLQueue which is comming from constructor arguman.
 * @author francium
 *
 */
public class RssFetcher {

	private static final Logger LOG = Logger.getLogger(RssFetcher.class);
	private static final Logger LOG_PERFORMANCE = Logger.getLogger("main");

	private BlockingQueue<URL> sharedURLQueue;
	private BlockingQueue<Feed> sharedFeedQueue;
	/**
	 * 
	 * @param sharedURLQueue A blockedQueue which contains urls.
	 * @param sharedFeedQueue A blockedQueue which contains Feeds(URL,ArrayList(messages))
	 */
	public RssFetcher(BlockingQueue<URL> sharedURLQueue, BlockingQueue<Feed> sharedFeedQueue) {
		this.sharedURLQueue = sharedURLQueue;
		this.sharedFeedQueue = sharedFeedQueue;
	}
	/**
	 * It read urls from sharedUrlQueue one by one until sharedURLQueue is empty.
	 */
	public void fetch() {
		while (!sharedURLQueue.isEmpty()) {
			try {
				long time = System.currentTimeMillis();
				// önce al oku sonra feed object oalrak kuyruğa ekle
				URL url = sharedURLQueue.take();
				LOG.info("Producing feeds for this url :\t" + url);
				sharedFeedQueue.put(new Feed(url, new RssReader().read(url)));
				LOG_PERFORMANCE.error(System.currentTimeMillis()-time);
			} catch (InterruptedException e) {
				LOG.error(e);
			}
		}
	}
}
