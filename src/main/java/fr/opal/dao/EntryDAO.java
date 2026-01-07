package fr.opal.dao;

import fr.opal.type.Entry;
import java.util.List;

/**
 * Data Access Object interface for Entry persistence
 */
public interface EntryDAO {

    /**
     * Retrieves an entry by its ID
     */
    Entry getEntryById(int id);

    /**
     * Saves an entry to the database
     */
    void saveEntry(Entry entry);

    /**
     * Creates a new entry in the database
     */
    int createEntry(Entry entry);

    /**
     * Deletes an entry from the database
     */
    void deleteEntry(int id);

    /**
     * Retrieves all root entries (entries without parents)
     */
    List<Entry> getRootEntries();

    /**
     * Retrieves all child entries of a given parent
     */
    List<Entry> getChildEntries(int parentId);

    /**
     * Updates entry relationships in the database
     */
    void updateEntryRelationships(Entry entry);

    /**
     * Loads all entry data including comments and metadata
     */
    Entry loadEntryWithDetails(int id);
}
