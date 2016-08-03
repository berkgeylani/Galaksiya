package com.galaksiya.newsobserver.master;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.galaksiya.newsobserver.parser.FeedMessage;

public class Feed {

	private URL url;
	private BlockingQueue<FeedMessage> feedMessages;

	public Feed(URL url, BlockingQueue<FeedMessage> messages) {
		this.url = url;
		this.feedMessages = messages;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public BlockingQueue<FeedMessage> getFeedMessages() {
		return feedMessages;
	}

	public void setFeedMessages(BlockingQueue<FeedMessage> concurrentArrayQueue) {
		this.feedMessages = concurrentArrayQueue;
	}

	public boolean isEmpty() {
		if (url == null || feedMessages == null || url.toString().equals("") || feedMessages.isEmpty()) {
			return true;
		}
		return false;
	}
}
