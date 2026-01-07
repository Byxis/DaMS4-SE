package fr.opal.facade;

import fr.opal.type.Entry;
import fr.opal.type.User;
import fr.opal.type.Comment;
import fr.opal.dao.EntryDAO;
import fr.opal.factory.AbstractEntryFactory;

/**
 * Facade for entry operations
 * Provides simplified interface for loading, saving, and managing entries
 */
public class EntryFacade {

    private static EntryFacade instance;
    private AbstractEntryFactory factory;
    private EntryDAO dao;

    /**
     * Private constructor for singleton pattern
     */
    private EntryFacade() {
        this.factory = AbstractEntryFactory.getInstance();
        this.dao = factory.getEntryDAO();
    }

    /**
     * Gets the singleton instance
     */
    public static EntryFacade getInstance() {
        if (instance == null) {
            instance = new EntryFacade();
        }
        return instance;
    }

    /**
     * Loads an entry by its ID with all details
     */
    public Entry loadEntry(int id) {
        return dao.loadEntryWithDetails(id);
    }

    /**
     * Saves an entry to the database
     */
    public void saveEntry(Entry entry) {
        if (entry.getId() == 0) {
            int id = dao.createEntry(entry);
            entry.setId(id);
        } else {
            dao.saveEntry(entry);
        }
        dao.updateEntryRelationships(entry);
    }

    /**
     * Creates a new entry
     */
    public Entry createEntry(String title, String content, User author) {
        Entry entry = factory.createEntry(title, content, author);
        saveEntry(entry);
        return entry;
    }

    /**
     * Deletes an entry
     */
    public void deleteEntry(int id) {
        dao.deleteEntry(id);
    }

    /**
     * Gets the root entries (project roots)
     */
    public java.util.List<Entry> getRootEntries() {
        return dao.getRootEntries();
    }

    /**
     * Gets child entries of a parent
     */
    public java.util.List<Entry> getChildEntries(int parentId) {
        return dao.getChildEntries(parentId);
    }

    /**
     * Adds a comment to an entry and persists it
     */
    public void addComment(Entry entry, Comment comment) {
        entry.addComment(comment);
        saveEntry(entry);
    }

    /**
     * Removes a comment from an entry
     */
    public void removeComment(Entry entry, Comment comment) {
        entry.removeComment(comment);
        saveEntry(entry);
    }

    /**
     * Updates entry parent relationship
     */
    public void updateParentEntry(Entry entry, Entry newParent) throws Entry.CircularDependencyException {
        entry.setParentEntry(newParent);
        saveEntry(entry);
        if (newParent != null) {
            saveEntry(newParent);
        }
    }

    /**
     * Adds a child entry
     */
    public void addChildEntry(Entry parent, Entry child) throws Entry.CircularDependencyException {
        parent.addChildEntry(child);
        saveEntry(parent);
        saveEntry(child);
    }

    /**
     * Removes a child entry
     */
    public void removeChildEntry(Entry parent, Entry child) {
        parent.removeChildEntry(child);
        saveEntry(parent);
        saveEntry(child);
    }
}
