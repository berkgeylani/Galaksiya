package com.galaksiya.newsObserver.master;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.galaksiya.newsObserver.parser.FeedMessage;

public class WordProcessor {

	/**
	 * It takes concat of title and description and delete the tokens inside
	 * this,make lower case,split it according to " "
	 * 
	 * @param text
	 *            It is concat of title and description.
	 * @return It returns hash table which occurs from word and frequency.
	 */
	public Hashtable<String, Integer> splitWords(String... textArray) {
		Hashtable<String, Integer> wordFrequency = new Hashtable<String, Integer>();
		for (int i = 0; i < textArray.length; i++) {
			String text = textArray[i];
			List<String> processedStr = Arrays
					.asList(text.replaceAll("\\<[^>]*>", "").replaceAll("\\p{P}", " ").toLowerCase().split("\\s+"));
			Set<String> uniqueWords = new HashSet<String>(processedStr);
			for (String word : uniqueWords) {
				int frequency = Collections.frequency(processedStr, word);
				wordFrequency.put(word, frequency);
			}
		}
		return wordFrequency;
	}

	public FeedMessage cleanTextFields(FeedMessage message) {
		message.setDescription(
				message.getDescription().replaceAll("\\<[^>]*>", "").replaceAll("\\p{P}", "").toLowerCase());
		message.setTitle(message.getTitle().replaceAll("\\<[^>]*>", "").replaceAll("\\p{P}", "").toLowerCase());
		return message;
	}
}