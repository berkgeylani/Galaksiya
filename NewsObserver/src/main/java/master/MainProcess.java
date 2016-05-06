package master;
import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import database.MongoDb;
import rssparser.FeedMessage;
import rssparser.RssReader;

public class MainProcess {
	private	Hashtable<String, Hashtable<String, Integer>> mainHashStatistics = new Hashtable<String, Hashtable<String, Integer>>();
	private static final Logger LOG = Logger.getLogger(MainProcess.class);

	public MainProcess(){
		MongoDb mongoHelper = new MongoDb();
		mongoHelper.delete();//before get all the data from rss links we should clean our database.
	}
	
	public Hashtable<String, String> getResult(ArrayList<URL> rssLinkAL) {
		 Hashtable<String, String> lastNews = new Hashtable<String, String>();
		 Hashtable<String, Integer> wordFrequencyPerNew = new Hashtable<String,Integer>();
		
		 int i = 0;//check for the first new for all rss link
				for(URL rssURLs : rssLinkAL) {//for all link  ---new
					i=0;
					RssReader parserOfRss = new RssReader();//--new
					
				    WordProcessor processOfWords = new WordProcessor();
			    
				    for (FeedMessage message : parserOfRss.feedParser(rssURLs)) {//for all new  -----new
				      i++;
			          if(i==1) lastNews.put(rssURLs.toString(),message.getTitle());
			          String datePerNew=null;										
			          try {
			        	  if(message.getpubDate().length()==29)
			        		  datePerNew=message.getpubDate().substring(8, 10)+" "+message.getpubDate().toString().substring(4, 7)+" "+message.getpubDate().toString().substring(25, 29);//date of new
			        	  else
			        		  datePerNew=message.getpubDate().substring(8, 10)+" "+message.getpubDate().toString().substring(4, 7)+" "+message.getpubDate().toString().substring(24, 28);//date of new

			        	  //Mon May 02 20:03:40 EEST 2016       -456-Month  //-89-day //25-8 year  2016-01-21
			        	  //Tue Mar 22 14:15:00 EET 2016   EET,EEST,       2016-01-21
			        	  //Tue May 03 21:58:31 EEST 2016
			        	  //Tue May 03 23:25:52 EEST 2016
			          } catch (Exception e) {
						LOG.error(message.getpubDate().toString()+"Substring process problem.",e);
			          }
			          if(datePerNew=="02 May 2016") {
			        	  System.out.println("wqerq");
			          }
			          wordFrequencyPerNew=processOfWords.splitAndHashing(message.getTitle()+ " " +  message.getDescription());//It Brıng us hashtable which contains word and freq hashtable per new
			          
			          if(mainHashStatistics.containsKey(datePerNew)) {//if date has already saved we should increment frequency on these records
				          Enumeration<String> e = wordFrequencyPerNew.keys();
				          while (e.hasMoreElements()) { //check key value per new to increment or add  
				             String key = (String) e.nextElement();//key:key   wordFrequencyPerNew.get(key):value
				             if(mainHashStatistics.get(datePerNew).containsKey(key)){ //check word if yes we increment
				            	 mainHashStatistics.get(datePerNew).put(key, mainHashStatistics.get(datePerNew).get(key)+wordFrequencyPerNew.get(key));//before plus is old value and we add new value with 2nd arg
				             }else{
				            	 mainHashStatistics.get(datePerNew).put(key, wordFrequencyPerNew.get(key));//add a word and value
				             }
				          }
				       }else{  //no record on this date,then add our hash list directly
				    	   mainHashStatistics.put(datePerNew, wordFrequencyPerNew);
				       }
					   }
		    
		}
		return lastNews;
	}
	
		public void postToDatabase() {
			
			MongoDb mongoHelper = new MongoDb();

			Enumeration<String> WordFreqForAllDay = mainHashStatistics.keys();//for all days
	        while (WordFreqForAllDay.hasMoreElements()) { //It provides us to travel hashlist per day
	        	String keyDay = (String) WordFreqForAllDay.nextElement();//key:key   mainHashStatistics.get(key):value(Hashlist for this day)
				Enumeration<String> wordFreqPerDay = mainHashStatistics.get(keyDay).keys();//for just one day
		        while (wordFreqPerDay.hasMoreElements()) { //check key value per new to increment or add  
		           String keyWord = (String) wordFreqPerDay.nextElement();//key:word   mainHashStatistics.get(keyDay).get(key): frequency
		           mongoHelper.save(keyDay, keyWord, mainHashStatistics.get(keyDay).get(keyWord));
		        }
	        }
	        LOG.debug("Posted the database");
	        
	     }
	
	
	
}