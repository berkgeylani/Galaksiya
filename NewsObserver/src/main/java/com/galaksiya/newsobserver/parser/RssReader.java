package com.galaksiya.newsobserver.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class RssReader {

	private static final Logger LOG = Logger.getLogger(RssReader.class);

	private ArrayList<FeedMessage> itemsAL = new ArrayList<>();

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
	public ArrayList<FeedMessage> parseFeed(URL url) {
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
				message.setTitle(entry.getTitle());
				message.setDescription(entry.getDescription().getValue());
				message.setPubDate(entry.getPublishedDate().toString());
				message.setLink(entry.getLink().toString());
				message.toString();
				itemsAL.add(message);
			}
			if (itemsAL.isEmpty()) {
				LOG.error("There is no news.From this rss : -> "+url);
			}
		} catch (IllegalArgumentException | IOException e) {
			LOG.error("RSS reader problem.  ->"+url, e);
		} catch (FeedException e) {
			LOG.error("Source to feed process is failed.->   "+url, e);
		}
		return itemsAL;
	}

}