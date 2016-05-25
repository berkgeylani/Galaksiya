package com.galaksiya.newsObserver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.galaksiya.newsObserver.database.DatabaseTestSuite;
import com.galaksiya.newsObserver.master.MasterTestSuite;
import com.galaksiya.newsObserver.parser.RssReaderTest;

@RunWith(Suite.class)
@SuiteClasses({
	DatabaseTestSuite.class,
	MasterTestSuite.class,
	RssReaderTest.class
})
public class AllTests {
}
