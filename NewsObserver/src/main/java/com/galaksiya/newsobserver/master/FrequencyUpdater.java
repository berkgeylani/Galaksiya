package com.galaksiya.newsobserver.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsobserver.database.Database;

public class FrequencyUpdater {

	private static final Logger LOG = Logger.getLogger(FrequencyUpdater.class);

	private static final Object AD_UPDATE_INSERT = new Object();

	private Database database;

	public FrequencyUpdater(Database dbObject) {
		this.database = dbObject;
	}

	/**
	 * It takes date-word and frequency then if it is in database,it
	 * increments.If not it inserts it.
	 * 
	 * @param docL
	 * @return flagSuccessful Successful Flag(True: Okay False : Fault)
	 */
	public boolean addDatabase(List<Document> docList) {
		if (docList == null || docList.isEmpty())
			return false;
		boolean isSuccessfulAll = true;
		boolean isSuccessfulPerProcess = true;
		boolean isThereAnyDataToInsert = false;
		List<Document> insertList = new ArrayList<>();
		synchronized (AD_UPDATE_INSERT) {
			for (Document document : docList) {
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
		}
		return isSuccessfulAll;
	}
}
