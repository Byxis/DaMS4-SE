package fr.opal.facade;

import fr.opal.type.Entry;
import fr.opal.type.User;
import fr.opal.type.Comment;
import fr.opal.type.EntryContextDTO;
import fr.opal.service.EntryManager;
import java.util.List;

/**
 * Facade for entry operations
 * Thin wrapper that delegates to EntryManager
 * Acts as the entry point from Controller to Service layer
 */
public class EntryFacade {

    private static EntryFacade instance;
    private EntryManager manager;

    /**
     * Private constructor for singleton pattern
     */
    private EntryFacade() {
        this.manager = new EntryManager();
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
     * Loads an entry by its ID with Depth-1 context (parent and children metadata)
     * Returns an EntryContextDTO containing target entry, parent, and children
     */
    public EntryContextDTO loadEntry(int id) {
        return manager.getEntry(id);
    }

    /**
     * Saves an entry to the database
     */
    public void saveEntry(Entry entry) {
        manager.persistEntry(entry);
    }

    /**
     * Creates a new entry
     */
    public Entry createEntry(String title, String content, User author) {
        return manager.createNewEntry(title, content, author);
    }

    /**
     * Deletes an entry
     */
    public void deleteEntry(int id) {
        manager.removeEntry(id);
    }

    /**
     * Gets the root entries (project roots)
     */
    public List<Entry> getRootEntries() {
        return manager.getAllRootEntries();
    }

    /**
     * Gets child entries of a parent
     */
    public List<Entry> getChildEntries(int parentId) {
        return manager.getChildrenOfEntry(parentId);
    }

    /**
     * Adds a comment to an entry and persists it
     */
    public void addComment(Entry entry, Comment comment) {
        manager.persistCommentToEntry(entry, comment);
    }

    /**
     * Removes a comment from an entry
     */
    public void removeComment(Entry entry, Comment comment) {
        manager.deleteCommentFromEntry(entry, comment);
    }

    /**
     * Updates entry parent relationship
     */
    public void updateParentEntry(Entry entry, Entry newParent) throws Entry.CircularDependencyException {
        manager.updateEntryParent(entry, newParent);
    }

    /**
     * Adds a child entry
     */
    public void addChildEntry(Entry parent, Entry child) throws Entry.CircularDependencyException {
        manager.attachChildToParent(parent, child);
    }

    /**
     * Removes a child entry
     */
    public void removeChildEntry(Entry parent, Entry child) {
        manager.detachChildFromParent(parent, child);
    }

    /**
     * Creates a complete placeholder entry structure with all test variations
     * @param ownerUsername The username of the entry owner (e.g., "lez")
     * @return The root placeholder entry with full hierarchy
     */
    public Entry createPlaceholderEntryStructure(String ownerUsername) throws Exception {
        return manager.initializePlaceholderStructure(ownerUsername);
    }

    /**
     * Ensures the placeholder entry structure exists in the database
     * @param ownerUsername The username of the entry owner (e.g., "lez")
     * @return The root entry ID if created/loaded successfully, 0 if operation failed
     */
    public int ensurePlaceholderEntryExists(String ownerUsername) throws Exception {
        return manager.ensurePlaceholderStructureExists(ownerUsername);
    }

    /**
     * Loads the root placeholder entry from the database with Depth-1 context
     * @return EntryContextDTO with root entry and its children, null if not found
     */
    public EntryContextDTO loadPlaceholderRootFromDatabase() {
        return manager.fetchPlaceholderRootFromDatabase();
    }
}

