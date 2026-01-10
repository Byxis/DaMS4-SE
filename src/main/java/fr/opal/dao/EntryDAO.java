package fr.opal.dao;

import fr.opal.type.Entry;
import java.util.List;

/**
 * Abstract DAO for Entry persistence operations
 * Follows the same pattern as UserDAO from main branch
 */
public abstract class EntryDAO {

    /**
     * Retrieves an entry by its ID
     */
    public abstract Entry getEntryById(int id);

    /**
     * Saves an entry to the database
     */
    public abstract void saveEntry(Entry entry);

    /**
     * Creates a new entry in the database
     */
    public abstract int createEntry(Entry entry);

    /**
     * Deletes an entry from the database
     */
    public abstract void deleteEntry(int id);

    /**
     * Retrieves all root entries (entries without parents)
     */
    public abstract List<Entry> getRootEntries();

    /**
     * Retrieves all child entries of a given parent
     */
    public abstract List<Entry> getChildEntries(int parentId);

    /**
     * Updates entry relationships in the database
     */
    public abstract void updateEntryRelationships(Entry entry);

    /**
     * Loads all entry data including comments and metadata
     */
    public abstract Entry loadEntryWithDetails(int id);
}
