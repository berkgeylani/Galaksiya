package com.galaksiya.newsObserver.master;

import static org.junit.Assert.*;

import org.junit.Test;

public class DateUtilsTest {
	private DateUtils dateUtils = new DateUtils();
	@Test
	public void canConvert() {
		assertTrue(dateUtils.canConvert("02-May-2016"));
		assertTrue(dateUtils.canConvert("02-May 2016"));
		assertTrue(dateUtils.canConvert("02 May 2016"));
	}
	@Test
	public void canConvertInvalidInput() {
		assertFalse(dateUtils.canConvert("02 May3 2016"));
	}
	@Test 
	public void canConvertWithoutBlankInput(){
		assertFalse(dateUtils.canConvert("21 May!2011"));
	}
	@Test
	public void dateCustomizeValidInput() {
		assertEquals("13 May 2016", dateUtils.dateCustomize("Fri May 13 10:24:56 EEST 2016"));
		assertEquals("22 Mar 2016", dateUtils.dateCustomize("Tue Mar 22 14:15:00 EET 2016"));
	}

}
