package fr.opal.factory;

import fr.opal.type.Entry;
import fr.opal.type.User;

/**
 * MySQL implementation of EntryFactory.
 * Connection management is delegated to AbstractDAOFactory.
 */
public class MySQLEntryFactory extends AbstractEntryFactory {

    /**
     * Constructor
     */
    public MySQLEntryFactory() {
        super();
    }

    /**
     * Creates a new Entry
     */
    @Override
    public Entry createEntry(String title, String content, User author) {
        return new Entry(title, content, author);
    }

    /**
     * Creates an Entry with an ID (for loading from database)
     */
    @Override
    public Entry createEntry(int id, String title, String content, User author) {
        return new Entry(id, title, content, author);
    }
}
