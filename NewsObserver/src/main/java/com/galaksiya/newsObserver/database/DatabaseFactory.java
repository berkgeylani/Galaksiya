package com.galaksiya.newsObserver.database;

public class DatabaseFactory {
	private static DatabaseFactory databaseFactory;
	
	private String databaseType;

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public static DatabaseFactory getInstance() {
		if (databaseFactory == null) {
			synchronized (DatabaseFactory.class) {
				if (databaseFactory == null) { // yes double check
					databaseFactory = new DatabaseFactory();
				}
			}
		}
		return databaseFactory;
	}
	public static void setInstance(DatabaseFactory dFactory){
		databaseFactory=dFactory;
	}
	public Database getDatabase(){
		if (databaseType.equalsIgnoreCase("mongo")) {
			return new MongoDb();
		} else if(databaseType.equalsIgnoreCase("derby")){
			return new DerbyDb();
		}
		return null;
	}
	public Database getDatabase(String databaseTypeParameter){
		if (databaseType.equalsIgnoreCase("mongo")) {
			return new MongoDb(databaseTypeParameter);
		} else if(databaseType.equalsIgnoreCase("derby")){
			return new DerbyDb(databaseTypeParameter);
		}
		return null;
	}
}
