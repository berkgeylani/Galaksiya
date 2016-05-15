package database;


public interface Database {
	
	public boolean save(String dateStr,String word,int frequency);//which is insert opereation
	
	public void delete();//which is remove opereation
	
	public boolean update(String dateStr,String word,int frequency);//which is changing data opereation
	
	public void fetch(String dateStr);//which is getting data

}