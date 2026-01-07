package fr.opal.factory;

import fr.opal.type.Entry;
import fr.opal.type.User;

/**
 * Abstract factory for creating Entry instances
 */
public abstract class AbstractEntryFactory {

    protected static AbstractEntryFactory instance;

    /**
     * Gets the singleton instance
     */
    public static AbstractEntryFactory getInstance() {
        if (instance == null) {
            instance = new MySQLEntryFactory();
        }
        return instance;
    }

    /**
     * Creates a new Entry
     */
    public abstract Entry createEntry(String title, String content, User author);

    /**
     * Creates an Entry with an ID (for loading from database)
     */
    public abstract Entry createEntry(int id, String title, String content, User author);

    /**
     * Gets the DAO for the factory's storage type
     */
    public abstract fr.opal.dao.EntryDAO getEntryDAO();
}
