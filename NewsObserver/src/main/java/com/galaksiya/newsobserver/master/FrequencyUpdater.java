package com.galaksiya.newsobserver.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsobserver.database.Database;

/**
 * This class is controll a data which is already inserted or not,then,if
 * not,insert,else update.
 * 
 * @author francium
 *
 */
public class FrequencyUpdater {

	private final static Logger LOG = Logger.getLogger(FrequencyUpdater.class);

	private static final Object AD_LOCK_UPDATE_INSERT = new Object();

	private Database database;

	/**
	 * It gets a database object for using.
	 * 
	 * @param dbObject
	 */
	public FrequencyUpdater(Database dbObject) {
		this.database = dbObject;
	}

	/**
	 * It takes date-word and frequency then if it is in database,it
	 * increments.If not it inserts it.
	 * 
	 * @param dateWordFrequencyListPerNew
	 *            This is a list which containg dateiword,frequency.
	 * @return flagSuccessful Successful Flag(True: Okay False : Fault)
	 */
	public boolean addDatabase(List<Document> dateWordFrequencyListPerNew) {
		if (dateWordFrequencyListPerNew == null || dateWordFrequencyListPerNew.isEmpty())
			return false;
		boolean isSuccessfulAll = true;
		boolean isSuccessfulPerProcess = true;
		boolean isThereAnyDataToInsert = false;
		List<Document> insertList = new ArrayList<>();
		long time = System.currentTimeMillis();
		synchronized (AD_LOCK_UPDATE_INSERT) {
			for (Document document : dateWordFrequencyListPerNew) {
				long contain = database.contain(document.getDate("date"), document.getString("word"));
				if (contain >= 1) {
					isSuccessfulPerProcess = database.update(document.getDate("date").toString(), // 2
							document.getString("word"), document.getInteger("frequency"));
					isSuccessfulAll = isSuccessfulAll && isSuccessfulPerProcess; // TODO boolean&=boolean oluyormu bak
				} else if (contain == -1) {
					isSuccessfulAll = false;
					continue;
				} else {
					isThereAnyDataToInsert = true;
					insertList.add(document);
				}
			}
			if (isThereAnyDataToInsert) {
				isSuccessfulPerProcess = database.saveMany(insertList);// 1
				isSuccessfulAll = isSuccessfulAll && isSuccessfulPerProcess;
			}
			LOG.debug("Time: \t " + (System.currentTimeMillis() - time));
		}
		return isSuccessfulAll;
	}
}
