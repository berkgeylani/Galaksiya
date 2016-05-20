package com.galaksiya.newsObserver.mainTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.galaksiya.newsObserver.database.DatabaseTestSuite;
import com.galaksiya.newsObserver.master.MasterTestSuite;

@RunWith(Suite.class)
@SuiteClasses({
	DatabaseTestSuite.class,
	MasterTestSuite.class
})
public class MainTestClass {

	

}
