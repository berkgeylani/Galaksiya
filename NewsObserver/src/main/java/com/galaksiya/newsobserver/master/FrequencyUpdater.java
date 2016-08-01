package com.galaksiya.newsobserver.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.galaksiya.newsobserver.database.Database;

public class FrequencyUpdater {

	private final static Logger LOG = Logger.getLogger("com.newsobserver.admin");
	
	private Database database;

	public FrequencyUpdater(Database dbObject) {
		this.database = dbObject;
	}

	/**
	 * It takes date-word and frequency then if it is in database,it
	 * increments.If not it inserts it.
	 * 
	 * @param datePerNew
	 *            Date which will be search.
	 * @param word
	 *            Word which will be search.
	 * @param frequency
	 *            Frequency of a word.
	 * @return flagSuccessful Successful Flag(True: Okay False : Fault)
	 */
	public boolean addDatabase(List<Document> docList) {
		if (docList == null || docList.isEmpty())
			return false;
		boolean isSuccessfulAll = true;
		boolean isSuccessfulPerProcess = true;
		boolean alreadyInsertedToDatabase = false;
		List<Document> insertList = new ArrayList<>();

		for (Document document : docList) {
			if (database.contain(document.getDate("date"),document.getString("word")) >= 1) {
				isSuccessfulPerProcess = database.update(document.getDate("date").toString(),  //2
						document.getString("word"), document.getInteger("frequency"));
				isSuccessfulAll = isSuccessfulAll && isSuccessfulPerProcess; // TODO boolean&=boolean oluyormu bak
			} else {
				insertList.add(document);
			}
		}
		if (!alreadyInsertedToDatabase) {
			isSuccessfulPerProcess = database.saveMany(insertList);//1
			isSuccessfulAll = isSuccessfulAll && isSuccessfulPerProcess;
		}
		return isSuccessfulAll;
	}
}
