package com.galaksiya.newsobserver.database;

public class DatabaseFactory {

	private static final String DEFAULT_DATABASE_TYPE = DatabaseConstants.DATABASE_TYPE_DERBY;

	private static DatabaseFactory databaseFactory;

	private String databaseType = DEFAULT_DATABASE_TYPE;

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public void setDefaultDatabaseType() {
		setDatabaseType(DEFAULT_DATABASE_TYPE);
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

	public static void setInstance(DatabaseFactory dFactory) {
		databaseFactory = dFactory;
	}

	public Database getDatabase() {
		return getDatabase(null);
	}

	public Database getDatabase(String databaseTypeParameter) {
		if (databaseType.equalsIgnoreCase(DatabaseConstants.DATABASE_TYPE_MONGO)) {
			return new MongoDb(databaseTypeParameter);
		} else if (databaseType.equalsIgnoreCase(DEFAULT_DATABASE_TYPE)) {
			return new DerbyDb(databaseTypeParameter);
		}
		return null;
	}
}
