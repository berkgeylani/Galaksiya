package com.galaksiya.newsobserver.database;
/**
 * It create a database object which is selected with arguments.
 * @author francium
 *
 */
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
/**
 * 
 * @param databaseCollectionParameter It select a database table/collection.
 * @return Return a database which is seleected with arguments.
 */
	public Database getDatabase(String databaseCollectionParameter) {
		if (databaseType.equalsIgnoreCase(DatabaseConstants.DATABASE_TYPE_MONGO)) {
			return new MongoDb(databaseCollectionParameter);
		} else if (databaseType.equalsIgnoreCase(DEFAULT_DATABASE_TYPE)) {
			return new DerbyDb(databaseCollectionParameter);
		}
		return null;
	}
}
