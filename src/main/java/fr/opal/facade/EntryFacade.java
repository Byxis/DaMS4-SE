package fr.opal.facade;

import fr.opal.type.Entry;
import fr.opal.type.EPermission;
import fr.opal.type.User;
import fr.opal.type.Message;
import fr.opal.type.EntryContextDTO;
import fr.opal.service.EntryManager;

import java.io.File;
import java.util.List;

/**
 * Facade for entry operations
 * Strict interface between Controller (UI) and Manager (Business) layers
 * Hides complexity of underlying Managers from the Controller
 * Controllers must ONLY interact with this facade - never directly with Managers
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

    // ==================== Entry State Management ====================

    /**
     * Gets the currently selected entry
     */
    public Entry getCurrentEntry() {
        return manager.getCurrentEntry();
    }

    /**
     * Sets the currently selected entry
     */
    public void setCurrentEntry(Entry entry) {
        manager.setCurrentEntry(entry);
    }

    // ==================== Entry Loading ====================

    /**
     * Loads an entry by its ID with Depth-1 context (parent and children metadata)
     * Returns an EntryContextDTO containing target entry, parent, and children
     */
    public EntryContextDTO loadEntry(int id) {
        return manager.getEntry(id);
    }

    /**
     * Loads an entry with access permission check
     * @throws AccessDeniedException if user does not have access
     */
    public EntryContextDTO loadEntryWithAccessCheck(int entryId, User user) throws AccessDeniedException {
        try {
            manager.setCurrentUser(user);
            return manager.getEntryWithAccessCheck(entryId);
        } catch (EntryManager.PermissionException e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

    /**
     * Loads the initial project entry by name
     * Used during controller initialization
     */
    public EntryContextDTO loadInitialProject(String projectName) {
        return manager.loadProjectByName(projectName);
    }

    // ==================== Entry UI State ====================

    /**
     * Gets the UI state for an entry based on user permissions
     * Used by Controller to determine which UI elements to enable/disable
     */
    public EntryUIState getEntryUIState(Entry entry, User user) {
        return manager.getUIStateForEntry(entry, user);
    }

    /**
     * DTO representing UI state based on permissions
     */
    public static class EntryUIState {
        private final boolean canView;
        private final boolean canComment;
        private final boolean canEdit;

        public EntryUIState(boolean canView, boolean canComment, boolean canEdit) {
            this.canView = canView;
            this.canComment = canComment;
            this.canEdit = canEdit;
        }

        public boolean canView() { return canView; }
        public boolean canComment() { return canComment; }
        public boolean canEdit() { return canEdit; }
    }

    // ==================== Entry CRUD Operations ====================

    /**
     * Saves an entry to the database
     */
    public void saveEntry(Entry entry) {
        manager.persistEntry(entry);
    }

    /**
     * Updates entry content (title and content only)
     * @throws PermissionDeniedException if user lacks EDITOR permission
     */
    public void updateEntryContent(int entryId, String title, String content, User user) 
            throws PermissionDeniedException {
        try {
            manager.setCurrentUser(user);
            manager.updateEntryContent(entryId, title, content);
        } catch (EntryManager.PermissionException e) {
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    /**
     * Creates a new entry as a child of the given parent
     */
    public Entry createChildEntry(Entry parent, String title, String content, User author) 
            throws Entry.CircularDependencyException {
        Entry newEntry = manager.createNewEntry(title, content, author);
        manager.attachChildToParent(parent, newEntry);
        return newEntry;
    }

    /**
     * Creates a new standalone entry
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

    // ==================== Entry Navigation ====================

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
     * Navigates to the parent entry
     * @throws PermissionDeniedException if user lacks permission
     */
    public EntryContextDTO navigateToParent(User user) throws PermissionDeniedException {
        try {
            manager.setCurrentUser(user);
            return manager.navigateToParent();
        } catch (EntryManager.PermissionException e) {
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    // ==================== Comment Management ====================

    /**
     * Adds a comment to an entry
     * Delegates message creation and permission checks to manager
     * @throws PermissionDeniedException if user lacks COMMENTOR permission
     */
    public void addComment(Entry entry, User user, String commentText) throws PermissionDeniedException {
        try {
            // Delegate all message creation to manager - no Message instantiation here
            manager.setCurrentUser(user);
            manager.addComment(entry.getId(), user, commentText);
            
            // Reload the entry with fresh messages from database
            EntryContextDTO refreshedContext = loadEntry(entry.getId());
            if (refreshedContext != null) {
                entry.getMessages().clear();
                entry.getMessages().addAll(refreshedContext.getTargetEntry().getMessages());
            }
        } catch (EntryManager.PermissionException e) {
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    /**
     * Removes a message from an entry's channel
     * @throws PermissionDeniedException if user lacks EDITOR permission
     */
    public void removeMessage(Entry entry, Message message, User user) throws PermissionDeniedException {
        try {
            manager.setCurrentUser(user);
            manager.deleteMessage(entry, message);
            
            // Reload the entry with fresh messages from database
            EntryContextDTO refreshedContext = loadEntry(entry.getId());
            if (refreshedContext != null) {
                entry.getMessages().clear();
                entry.getMessages().addAll(refreshedContext.getTargetEntry().getMessages());
            }
        } catch (EntryManager.PermissionException e) {
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    // ==================== Permission Management ====================

    /**
     * Sets a user's permission on an entry
     * @throws PermissionDeniedException if current user lacks EDITOR permission
     */
    public void setUserPermission(Entry entry, String username, EPermission permission, User currentUser) 
            throws PermissionDeniedException {
        try {
            manager.setCurrentUser(currentUser);
            manager.setUserPermissionByUsername(entry, username, permission);
        } catch (EntryManager.PermissionException e) {
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    // ==================== Entry Relationship Management ====================

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

    // ==================== Import/Export ====================

    /**
     * Imports an entry from a file
     * Delegates format determination and import to manager
     * @throws IllegalArgumentException for unsupported file formats
     */
    public Entry importEntry(File file) throws Exception {
        return manager.importEntryFromFile(file);
    }

    /**
     * Exports an entry to a file
     * Delegates format determination and export to manager
     * @throws IllegalArgumentException for unsupported file formats
     */
    public void exportEntry(Entry entry, File file) throws Exception {
        manager.exportEntryToFile(entry, file);
    }

    // ==================== Exception Classes ====================

    /**
     * Exception for access denied errors
     */
    public static class AccessDeniedException extends Exception {
        public AccessDeniedException(String message) {
            super(message);
        }
    }

    /**
     * Exception for permission denied errors
     */
    public static class PermissionDeniedException extends Exception {
        public PermissionDeniedException(String message) {
            super(message);
        }
    }
}

