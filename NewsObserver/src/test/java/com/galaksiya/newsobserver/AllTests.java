package com.galaksiya.newsobserver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.galaksiya.newsobserver.database.DatabaseTestSuite;
import com.galaksiya.newsobserver.master.MasterTestSuite;
import com.galaksiya.newsobserver.parser.RssReaderTest;

@RunWith(Suite.class)
@SuiteClasses({
	DatabaseTestSuite.class,
	MasterTestSuite.class,
	RssReaderTest.class
})
public class AllTests {
}
