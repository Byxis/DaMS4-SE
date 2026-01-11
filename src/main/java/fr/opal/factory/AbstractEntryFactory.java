package fr.opal.factory;

import fr.opal.dao.EntryDAO;
import fr.opal.type.Entry;
import fr.opal.type.User;

/**
 * Abstract factory for creating Entry instances.
 * DAO access is delegated to AbstractDAOFactory for consistency.
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
     * Gets the DAO for the factory's storage type.
     * Delegates to the centralized AbstractDAOFactory.
     */
    public EntryDAO getEntryDAO() {
        return AbstractDAOFactory.getFactory().createEntryDAO();
    }
}
