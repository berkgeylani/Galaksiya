package mastertest;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import master.FileParser;

public class FileParserTest {
	
	@Test
	public void test() {
		FileParser testFileParser = new FileParser("/home/francium/new.txt");
		Assert.assertNotEquals(0, testFileParser.getRssLinksAL().size(), 0.1);	
	}

}
