package com.galaksiya.newsObserver.master;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.galaksiya.newsObserver.database.MongoDb;
import com.galaksiya.newsObserver.parser.FeedMessage;
import com.galaksiya.newsObserver.parser.RssReader;

public class NewsChecker {

	private final static Logger LOG = Logger.getLogger(NewsChecker.class);
	private MongoDb mongoDbHelper = new MongoDb();
	
	public boolean updateActualNews(ArrayList<URL> RssLinksAL, Hashtable<String, String> lastNews) {
		//new.txt yolunu değiştir
		if( RssLinksAL == null || RssLinksAL.isEmpty() || lastNews == null || lastNews.isEmpty()) return false;
		for(URL rssURLs : RssLinksAL) {  //it read all rss urls
			if(containNewsTitle(lastNews.get(rssURLs), rssURLs)){
				LOG.error("There is no title in this rss.");
				return false;
				}
			travelInNews(lastNews, rssURLs);
			LOG.debug(rssURLs+" checked.");
		} 
		return true;
	}

	public void travelInNews(Hashtable<String, String> lastNews, URL rssURLs) {  
		String[] lastNewsArray =  new String[2];
		boolean updateNew = true,updated=false;
		RssReader parserOfRss=new RssReader();
		for (FeedMessage message : parserOfRss.feedParser(rssURLs)) {  //item to item reading
			boolean isThereAnyNewNews = !message.getTitle().equals(lastNews.get(rssURLs.toString()));
			if(isThereAnyNewNews) { //if there is a new news we should insert or increment it
				if(updateNew){
					lastNewsArray[0]=rssURLs.toString();
					lastNewsArray[1]=message.getTitle();
					updateNew=false;
					updated=true;
					}	
				handleMessage(message);
			} else {//if we come the lately new we can break
				if (updated){ 
					lastNews.put(lastNewsArray[0], lastNewsArray[1]);
					}
				break;
			}
		}
	}
	public Hashtable<String, Integer> handleMessage( FeedMessage message ) {
		WordProcessor processOfWords = new WordProcessor();
		Hashtable<String, Integer> wordFrequencyPerNew = new Hashtable<String, Integer>();
		String datePerNew=dateCustomize(message.getpubDate());
		wordFrequencyPerNew=processOfWords.splitAndHashing(message.getTitle()+ " " +  message.getDescription());//It Brıng us hashtable which contains word and freq hashtable per new
		//wordFrequency test edecez
		if(travelWordByWord(datePerNew,wordFrequencyPerNew))
			return null;
		return wordFrequencyPerNew;
	}
	
	public boolean containNewsTitle(String title,URL rssURLs){
		RssReader parserOfRss=new RssReader();
		for (FeedMessage message : parserOfRss.feedParser(rssURLs)) {  //item to item reading
			if(message.getTitle().equals(title)){
				return true;
			}
		}
		return false;
	}
	
	private boolean travelWordByWord(String datePerNew,Hashtable<String, Integer> wordFrequencyPerNew){
		DbHelper dbHelper = new DbHelper();
		boolean proccessSuccessful=false;
		Enumeration<String> e = wordFrequencyPerNew.keys();
		while (e.hasMoreElements()) { 
			String key = (String) e.nextElement();//key:key   wordFrequencyPerNew.get(key):value
			proccessSuccessful=dbHelper.addDatabase(datePerNew,key,wordFrequencyPerNew.get(key));
		}
		return proccessSuccessful;
	}
	
	public String dateCustomize(String pubDate){//buda duplicated mongoda date işlemleri yapan bir tane daha var bir clas açıp date işlemleri yapan yapmamız lazım
		String datePerNew;
		if(pubDate.length()==29)//Fri May 13 10:24:56 EEST 2016	13 May 2016
			datePerNew=pubDate.substring(8, 10)+" "+pubDate.toString().substring(4, 7)+" "+pubDate.toString().substring(25, 29);//date of new
		else
			datePerNew=pubDate.substring(8, 10)+" "+pubDate.toString().substring(4, 7)+" "+pubDate.toString().substring(24, 28);//date of new
		//Mon May 02 20:03:40 EEST 2016       -456-Month  //-89-day //25-8 year  2016-01-21
		//Tue Mar 22 14:15:00 EET 2016   EET,EEST,       2016-01-21
		return datePerNew;
	}
	
	public boolean canConvert(String datePerNew){
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yy");
		if(datePerNew.length()!=11) return false;
		try {
			format1.parse(datePerNew.replaceAll("\\s+", "-"));
			return true;
		} catch (ParseException e) {
			return false;
		}		
	}
}
