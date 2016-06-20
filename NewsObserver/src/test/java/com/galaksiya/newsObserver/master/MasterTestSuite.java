package com.galaksiya.newsObserver.master;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(Suite.class)
@SuiteClasses({
   NewsCheckerTest.class,
   FileParserTest.class,
   DbHelperTest.class,
   WebsiteContentCreatorTest.class
})
public class MasterTestSuite {
 
}

