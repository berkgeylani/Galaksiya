package com.galaksiya.newsobserver.parser;
/**
 * This is a POJO class.It consists from title,description,pubdate,link.
 * Save on it news.
 * @author francium
 *
 */
public class FeedMessage {

	private String title;
	private String description;
	private String pubDate;
	private String link;

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getpubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	@Override
	public String toString() {
		return "Feed [\n title = " + title + "\n description = " + description + " pubDate = " + pubDate
				+ "\n]\n_______________________________________________\n";
	}

}