package rssparser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import main.Main;


public class RssReader {
	
	private ArrayList<FeedMessage> itemsAL = new ArrayList<FeedMessage>();
	private static final Logger LOG = Logger.getLogger(RssReader.class);

	public ArrayList<FeedMessage> feedParser(URL url) {

		HttpURLConnection httpcon;
		try {
			httpcon = (HttpURLConnection) url.openConnection();
			// Reading the feed
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed;
			feed = input.build(new XmlReader(httpcon));
			List<SyndEntry> entries = feed.getEntries();
			Iterator<SyndEntry> itEntries = entries.iterator();
			while (itEntries.hasNext()) {
				FeedMessage message = new FeedMessage();
				SyndEntry entry = itEntries.next();
				message.setTitle(entry.getTitle());
				message.setDescription(entry.getDescription().getValue());
				message.setPubDate(entry.getPublishedDate().toString());
				message.toString();
				itemsAL.add(message);
			}
			if (!itemsAL.isEmpty()) {
				return itemsAL;
			} else {
				LOG.error("There is no news.");
				throw new EmptyStackException();
			}
		} catch (IllegalArgumentException | FeedException | IOException e) {
			LOG.error("RSS reader problem.",e);
		}
		return null;
	}
	
	public int getItemsCount() {
		
		return itemsAL.size();
	}
}