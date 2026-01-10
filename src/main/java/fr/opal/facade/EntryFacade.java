package fr.opal.facade;

import fr.opal.type.Entry;
import fr.opal.type.User;
import fr.opal.type.Message;
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
     * Adds a message (comment) to an entry's channel and persists it
     * Refreshes the entry's messages from database after persistence
     */
    public void addMessage(Entry entry, Message message) throws EntryManager.PermissionException {
        manager.addMessage(entry.getId(), message);
        
        // Reload the entry with fresh messages from database
        EntryContextDTO refreshedContext = loadEntry(entry.getId());
        if (refreshedContext != null) {
            entry.getMessages().clear();
            entry.getMessages().addAll(refreshedContext.getTargetEntry().getMessages());
        }
    }

    /**
     * Removes a message from an entry's channel
     * Refreshes the entry's messages from database after deletion
     */
    public void removeMessage(Entry entry, Message message) throws EntryManager.PermissionException {
        manager.deleteMessage(entry, message);
        
        // Reload the entry with fresh messages from database
        EntryContextDTO refreshedContext = loadEntry(entry.getId());
        if (refreshedContext != null) {
            entry.getMessages().clear();
            entry.getMessages().addAll(refreshedContext.getTargetEntry().getMessages());
        }
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
}

