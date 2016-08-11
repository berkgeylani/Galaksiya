package com.galaksiya.newsobserver.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
/**
 * It helps us to read rss.(Like xml reader)
 * @author francium
 *
 */
public class RssReader {

	private static final Logger LOG = Logger.getLogger(RssReader.class);

	/**
	 * It parse the rss which is given with param with using ROME library.It
	 * converts news to messages. These messages push to an arraylist to return.
	 * 
	 * @param url
	 *            The url which will be read
	 * @return Arraylist occurs from messages and these messages occurs from
	 *         news.
	 * @throws FeedException
	 */
	public BlockingQueue<FeedMessage> read(URL url) {
		BlockingQueue<FeedMessage> itemsAL = new LinkedBlockingQueue<>();
		if (url == null)
			return null;
		SyndFeed feed = null;
		InputStream is = null;
		try {
			URLConnection openConnection = url.openConnection();
			openConnection.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			is = openConnection.getInputStream();
			if ("gzip".equals(openConnection.getContentEncoding())) {
				is = new GZIPInputStream(is);
			}
			InputSource source = new InputSource(is);
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(source);
			@SuppressWarnings("unchecked")
			List<SyndEntry> entries = feed.getEntries();
			Iterator<SyndEntry> itEntries = entries.iterator();

			while (itEntries.hasNext()) {
				FeedMessage message = new FeedMessage();
				SyndEntry entry = itEntries.next();
				boolean titleCorrect = entry.getTitle() != null && entry.getTitle() != "";
				boolean descriptionCorrect = entry.getDescription() != null && entry.getDescription().getValue() != "";
				boolean pubDateCorrect = entry.getPublishedDate() != null && entry.getPublishedDate().toString() != "";
				boolean linkCorrect = entry.getLink() != null && entry.getLink() != "";
				if (titleCorrect && descriptionCorrect && pubDateCorrect && linkCorrect) {
					message.setTitle(entry.getTitle());
					message.setDescription(entry.getDescription().getValue());
					message.setPubDate(entry.getPublishedDate().toString());
					message.setLink(entry.getLink());
					message.toString();
					itemsAL.add(message);
				} else {
					LOG.error("Problem while reading item because of malformed rss item. -->\t" + url);
				}
			}
			if (itemsAL.isEmpty()) {
				LOG.error("There is no news.From this rss : -> " + url);
			}
		} catch (IllegalArgumentException | IOException e) {
			LOG.error("RSS reader problem.  ->" + url, e);
		} catch (FeedException e) {
			LOG.error("Source to feed process is failed.->   " + url, e);
		}
		return itemsAL;
	}

}