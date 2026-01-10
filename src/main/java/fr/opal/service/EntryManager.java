package fr.opal.service;

import fr.opal.type.Entry;
import fr.opal.type.User;
import fr.opal.type.Message;
import fr.opal.type.UserPermission;
import fr.opal.type.EPermission;
import fr.opal.type.EntryContextDTO;
import fr.opal.dao.EntryDAO;
import fr.opal.dao.ChannelDAO;
import fr.opal.factory.AbstractEntryFactory;
import fr.opal.factory.AbstractDAOFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry Manager Service
 * Contains entry-related business logic and persistence coordination
 * Owns the complexity: CRUD, relationships, permissions, placeholder creation
 * Delegates persistence to DAO
 * Uses unified channel architecture for messages (comments)
 */
public class EntryManager {

    private Entry currentEntry;
    private User currentUser;
    private AuthManager authManager;
    private EntryDAO dao;
    private ChannelDAO channelDAO;

    /**
     * Constructor with no parameters
     * Manager doesn't maintain current user - can handle any user
     */
    public EntryManager() {
        this.authManager = AuthManager.getInstance();
        this.dao = AbstractEntryFactory.getInstance().getEntryDAO();
        this.channelDAO = AbstractDAOFactory.getFactory().createChannelDAO();
        this.currentEntry = null;
        this.currentUser = null;
    }

    /**
     * Constructor with current user (for backward compatibility)
     */
    public EntryManager(User currentUser) {
        this();
        this.currentUser = currentUser;
    }

    // Entry Retrieval & Persistence

    /**
     * Loads an entry by ID with Depth-1 Lookahead context
     * Returns EntryContextDTO containing:
     * - Target Entry: Full details (content, permissions, comments)
     * - Parent Entry: Metadata only (ID, Title, Permissions) or null if root
     * - Child Entries: List of metadata only (ID, Title, Permissions)
     */
    public EntryContextDTO getEntry(int id) {
        // Load the target entry with full details and Depth-1 neighbors
        Entry targetEntry = dao.loadEntryWithDetails(id);
        
        if (targetEntry == null) {
            return null;
        }
        
        // Extract parent (Depth -1) - metadata only
        Entry parentEntry = targetEntry.getParentEntry();
        
        // Extract children (Depth +1) - already metadata only from DAO
        List<Entry> childEntries = targetEntry.getChildEntries();
        
        // Create and return the context DTO
        EntryContextDTO context = new EntryContextDTO(targetEntry, parentEntry, childEntries);
        
        // Store the target as current for backward compatibility
        this.currentEntry = targetEntry;
        
        return context;
    }

    /**
     * Persists an entry to the database
     * Handles both new entries (insert) and existing entries (update)
     */
    public void persistEntry(Entry entry) {
        if (entry.getId() == 0) {
            // New entry - insert and update ID
            int id = dao.createEntry(entry);
            entry.setId(id);
        } else {
            // Existing entry - update
            dao.saveEntry(entry);
        }
        // Update relationships
        dao.updateEntryRelationships(entry);
    }

    /**
     * Persists an entire entry structure recursively
     * Saves root and all descendants with all their data
     */
    public void persistEntryStructure(Entry rootEntry) {
        persistEntry(rootEntry);
        for (Entry child : rootEntry.getChildEntries()) {
            persistEntryStructure(child);
        }
    }

    /**
     * Creates a new entry and saves it immediately
     */
    public Entry createNewEntry(String title, String content, User author) {
        Entry entry = AbstractEntryFactory.getInstance().createEntry(title, content, author);
        persistEntry(entry);
        return entry;
    }

    /**
     * Removes an entry from the database
     */
    public void removeEntry(int id) {
        dao.deleteEntry(id);
    }

    /**
     * Gets all root entries from the database
     */
    public List<Entry> getAllRootEntries() {
        return dao.getRootEntries();
    }

    /**
     * Gets child entries of a parent from the database
     */
    public List<Entry> getChildrenOfEntry(int parentId) {
        return dao.getChildEntries(parentId);
    }

    // Navigation Methods

    /**
     * Gets the root of the entry tree
     */
    public Entry getRootEntry() {
        if (currentEntry == null) return null;
        return currentEntry.getRootEntry();
    }

    /**
     * Gets all root entries (project roots)
     */
    public List<Entry> getRootEntries() {
        return getAllRootEntries();
    }

    /**
     * Navigates to parent entry
     * Reloads parent from database with full Depth-1 context
     */
    public EntryContextDTO navigateToParent() throws PermissionException {
        if (currentEntry == null || currentEntry.getParentEntry() == null) {
            return null;
        }
        
        Entry parentMetadata = currentEntry.getParentEntry();
        if (!hasPermission(parentMetadata, EPermission.READER)) {
            throw new PermissionException("User does not have permission to view parent entry");
        }
        
        // Reload parent with full Depth-1 context (its own parent, children, etc.)
        return getEntry(parentMetadata.getId());
    }

    /**
     * Navigates to a child entry
     * Reloads child from database with full Depth-1 context
     */
    public EntryContextDTO navigateToChild(Entry child) throws PermissionException {
        if (!hasPermission(child, EPermission.READER)) {
            throw new PermissionException("User does not have permission to view this entry");
        }
        
        // Reload child with full Depth-1 context (its own parent, children, etc.)
        return getEntry(child.getId());
    }

    /**
     * Gets all child entries of the current entry
     */
    public List<Entry> getChildEntries() {
        if (currentEntry == null) return new ArrayList<>();
        return currentEntry.getChildEntries();
    }

    /**
     * Gets all descendants (flattened tree) of the current entry
     */
    public List<Entry> getDescendants() {
        if (currentEntry == null) return new ArrayList<>();
        return currentEntry.getDescendants();
    }

    // Entry Relationship Management

    /**
     * Changes the parent of an entry (with circular dependency check)
     */
    public void updateEntryParent(Entry entry, Entry newParent) throws Entry.CircularDependencyException {
        entry.setParentEntry(newParent);
        persistEntry(entry);
        if (newParent != null) {
            persistEntry(newParent);
        }
    }

    /**
     * Adds a child to an entry
     */
    public void attachChildToParent(Entry parent, Entry child) throws Entry.CircularDependencyException {
        parent.addChildEntry(child);
        persistEntry(parent);
        persistEntry(child);
    }

    /**
     * Removes a child from an entry
     */
    public void detachChildFromParent(Entry parent, Entry child) {
        parent.removeChildEntry(child);
        persistEntry(parent);
        persistEntry(child);
    }

    // Message Management (unified channel architecture)

    /**
     * Adds a message to an entry's channel with permission checks
     * Permission: COMMENTOR or EDITOR
     * Message is persisted immediately via ChannelDAO (Auto-Save)
     */
    public void addMessage(int entryId, Message message) throws PermissionException {
        // SECURITY: Verify user has permission to comment
        Entry entry = dao.loadEntryWithDetails(entryId);
        if (entry == null) {
            throw new PermissionException("Entry not found");
        }
        
        if (!hasPermission(entry, EPermission.COMMENTOR)) {
            throw new PermissionException("You do not have permission to comment on this entry");
        }
        
        // Ensure message has correct channel ID
        message.setChannelId(entry.getChannelId());
        
        // Persist message directly to unified channel
        channelDAO.saveMessage(message);
        
        // Update cached messages in entry
        entry.addMessage(message);
    }

    /**
     * Removes a message from entry's channel with permission checks
     * Permission: EDITOR (or message author)
     */
    public void deleteMessage(Entry entry, Message message) throws PermissionException {
        // SECURITY: Only EDITOR can remove messages
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("You do not have permission to delete comments");
        }
        
        // Delete from database
        channelDAO.deleteMessage(message.getId());
        
        // Update cached messages
        entry.removeMessage(message);
    }

    /**
     * Gets all messages for an entry from its channel
     */
    public List<Message> getMessages(Entry entry) {
        if (entry.getChannelId() > 0) {
            return channelDAO.getMessagesForChannel(entry.getChannelId());
        }
        return entry.getMessages();
    }

    // Entry Data & Permission Updates

    /**
     * Updates entry content (Manual Save)
     * Scope: Title and Content only
     * Permission: EDITOR only
     * SECURITY: READER and COMMENTOR users are strictly blocked
     */
    public void updateEntryContent(int entryId, String newTitle, String newContent) throws PermissionException {
        Entry entry = dao.loadEntryWithDetails(entryId);
        if (entry == null) {
            throw new PermissionException("Entry not found");
        }
        
        // Use cascading permission check
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("You do not have editor permissions for this entry");
        }
        
        // Update entry data
        entry.setTitle(newTitle);
        entry.setContent(newContent);
        
        // Persist the entry content changes
        persistEntry(entry);
    }

    /**
     * Updates entry data and permissions (Manual Save)
     * Scope: Title, Content, and Permissions
     * Permission: EDITOR only
     * SECURITY: READER and COMMENTOR users are strictly blocked
     * @deprecated Use updateEntryContent() and setUserPermission() separately
     */
    @Deprecated
    public void updateEntry(int entryId, String newTitle, String newContent, List<UserPermission> permissionOverrides) throws PermissionException {
        Entry entry = dao.loadEntryWithDetails(entryId);
        if (entry == null) {
            throw new PermissionException("Entry not found");
        }
        
        // Use cascading permission check
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("You do not have editor permissions for this entry");
        }
        
        // Update entry data
        entry.setTitle(newTitle);
        entry.setContent(newContent);
        
        // Update permissions if provided
        if (permissionOverrides != null && !permissionOverrides.isEmpty()) {
            for (UserPermission perm : permissionOverrides) {
                entry.getPermissionManager().addUserPermission(perm);
            }
        }
        
        // Persist the complete entry with all changes
        persistEntry(entry);
    }

    // Permission Management

    /**
     * Sets a user's permission on an entry by User object
     * Permission: EDITOR only - auto-saves immediately
     */
    public void setUserPermission(Entry entry, User user, EPermission permission) throws PermissionException {
        // SECURITY: Only EDITOR can modify permissions
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("You do not have editor permissions to modify permissions");
        }
        
        UserPermission up = new UserPermission(user, permission);
        entry.getPermissionManager().addUserPermission(up);
        persistEntry(entry);
    }

    /**
     * Sets a user's permission on an entry by username
     * Permission: EDITOR only - auto-saves immediately
     * Looks up the user by username and applies the permission
     */
    public void setUserPermissionByUsername(Entry entry, String username, EPermission permission) throws PermissionException {
        // SECURITY: Only EDITOR can modify permissions
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("You do not have editor permissions to modify permissions");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new PermissionException("Username cannot be empty");
        }
        if (permission == null) {
            throw new PermissionException("Permission cannot be null");
        }
        
        // Look up user by username
        User user = authManager.getUserByUsername(username);
        if (user == null) {
            throw new PermissionException("User not found: " + username);
        }
        
        // Set the permission (bypasses redundant permission check since we already verified)
        UserPermission up = new UserPermission(user, permission);
        entry.getPermissionManager().addUserPermission(up);
        persistEntry(entry);
    }

    /**
     * Checks if current user has permission on specific entry
     * Uses cascading permission check - walks up parent chain until permission is found
     */
    private boolean hasPermission(Entry entry, EPermission requiredPermission) {
        if (entry == null || requiredPermission == null) {
            return false;
        }
        
        // Use cascading permission check - walks up parent chain to permission boundary
        UserPermission userPerm = entry.getUserPermissionWithCascade(currentUser);
        if (userPerm == null) {
            return false;
        }
        
        EPermission userPermission = userPerm.getPermission();
        
        // Check if user permission meets or exceeds required permission
        if (requiredPermission == EPermission.READER) {
            return userPermission.canView();
        } else if (requiredPermission == EPermission.COMMENTOR) {
            return userPermission.canComment();
        } else if (requiredPermission == EPermission.EDITOR) {
            return userPermission.canEdit();
        }
        
        return false;
    }

    // Placeholder Entry Management

    /**
     * Gets the current entry
     */
    public Entry getCurrentEntry() {
        return currentEntry;
    }

    /**
     * Sets the current entry
     */
    public void setCurrentEntry(Entry entry) {
        this.currentEntry = entry;
    }

    /**
     * Exception for permission-related errors
     */
    public static class PermissionException extends Exception {
        public PermissionException(String message) {
            super(message);
        }
    }
}
