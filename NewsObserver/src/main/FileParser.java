package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class FileParser {

	private ArrayList<URL> rssLinksAL;
	private static final Logger LOG = Logger.getLogger(FileParser.class);

	public FileParser(String filePath) {
		rssLinksAL = new ArrayList<URL>();
		readerOfFile(filePath);
	}

	public void readerOfFile(String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader("/home/francium/new.txt"));) {// ANSWER : http://stackoverflow.com/questions/17650970/am-i-using-the-java-7-try-with-resources-correctly
			String rssLink;
			while ((rssLink = br.readLine()) != null) {
				// printing out each line in the file
				URL url=null;
				try{
					 url= new URL(rssLink);
				}
				catch (MalformedURLException e) {
					LOG.error("In file(.txt),One of links isn't a Url",e);
				} 
				if(url!=null)
					rssLinksAL.add(url);
			}
		} catch (FileNotFoundException e) {
			LOG.error("File Not Found In Given Path",e);
		} catch (IOException e) {
			LOG.error("Input or output problem",e);
		}
		
	}

	public ArrayList<URL> getRssLinksAL() {// Return URLs of rss links
		return rssLinksAL;
	}

}