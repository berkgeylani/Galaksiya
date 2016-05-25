package com.galaksiya.newsObserver.database;

public interface Database {
	
	public boolean save(String dateStr,String word,int frequency);//which is insert opereation
	
	public boolean delete();//which is remove opereation
	
	public boolean update(String dateStr,String word,int frequency);//which is changing data opereation

}