package com.galaksiya.newsobserver.master;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtils {
	static final Logger LOG = Logger.getLogger(DateUtils.class);
	
	/**
	 * It controls is given String can convertable to date.
	 * @param datePerNew String occurs date
	 * @return true :Success false :fail
	 */
	public boolean canConvert(String datePerNew) {
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yy");
		if (datePerNew.length() != 11)
			return false;
		try {
			format1.parse(datePerNew.replaceAll("\\s+", "-"));
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	/**
	 * It converts String to Date format to search in query.
	 * @param dateStr Given date in String format.
	 * @return Return Date format(dd-MMM-yy).
	 */
	public Date dateConvert(String dateStr) {
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yy");
		Date date = null;
			try {
				date = format1.parse(dateStr.replaceAll("\\s+", "-"));
			} catch (ParseException e) {
				LOG.error("Input(String) couldn't convert to date.It will be requested again. ", e);
				return null;
			} // date is the our object's date
		return date;
	}
	/**
	 * It convert a String which occurs date like 'Fri May 13 10:24:56 EEST 2016' to 13 May 2016.
	 * @param pubDate A date string like 'Fri May 13 10:24:56 EEST 2016'
	 * @return ıt returns a String like '13 May 2016'.(date-month-year)
	 */
	public String dateCustomize(String pubDate) {
		String datePerNew;
		if (pubDate.length() == 29)
			datePerNew = pubDate.substring(8, 10) + " " + pubDate.substring(4, 7) + " "
					+ pubDate.substring(25, 29);
		else if(pubDate.length() == 28)
			datePerNew = pubDate.substring(8, 10) + " " + pubDate.substring(4, 7) + " "
					+ pubDate.substring(24, 28);
		else if (pubDate.length() == 11) {
			datePerNew = pubDate.substring(0, 2) + " " + pubDate.substring(3, 6) + " "
					+ pubDate.substring(7, 11);
		}
		else if (pubDate.length() == 10) {
			datePerNew = pubDate.substring(8, 10) + " " + pubDate.substring(5, 7) + " "
					+ pubDate.substring(0, 4);
		}else {
			 datePerNew=null;
		}
		return datePerNew;
	}

	
}