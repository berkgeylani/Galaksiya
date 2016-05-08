package master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class FileParser {

	private ArrayList<URL> rssLinksAL;
	private static final Logger LOG = Logger.getLogger(FileParser.class);

	public FileParser(String filePath) {
		rssLinksAL = new ArrayList<URL>();
		readerOfFile(filePath);
	}

	public boolean readerOfFile(String filePath) {
		try {
			Paths.get(filePath);
		}catch(InvalidPathException | NullPointerException exception){
			LOG.error("Given pathway is null or invalid.");;
			return false;//path yoksa 0
		}
		try (BufferedReader br = new BufferedReader(new FileReader(filePath));) {// ANSWER : http://stackoverflow.com/questions/17650970/am-i-using-the-java-7-try-with-resources-correctly
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
			return true; // herşey okeyse return 1
		} catch (FileNotFoundException e) {
			LOG.error("File Not Found In Given Path",e);
		} catch (IOException e) {
			LOG.error("Input or output problem",e);
		}
		return false; //catch girdiyse return 2;
	}

	public ArrayList<URL> getRssLinksAL() {// Return URLs of rss links
		return rssLinksAL;
	}

}