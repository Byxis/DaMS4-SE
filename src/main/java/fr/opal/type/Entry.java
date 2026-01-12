package fr.opal.type;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a node in the project's hierarchical tree.
 * An Entry may contain text, comments (via channel), and relationships to parent and child entries.
 * Comments are stored in a unified channel - use channelId with ChannelDAO to access them.
 */
public class Entry {
    private int id;
    private String title;
    private String content;
    private Entry parentEntry;
    private ArrayList<Entry> childEntries;
    private int channelId;  // Unified channel for comments
    private List<Message> messages;  // Cached messages from channel (transient, not persisted here)
    private MetaData metadata;
    private User author;
    private EntryPermissionManager permissionManager;
    
    /**
     * Default constructor for database loading
     */
    public Entry() {
        this.childEntries = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.metadata = new MetaData();
        this.permissionManager = new EntryPermissionManager();
    }
    
    /**
     * Constructor for creating a new Entry
     */
    public Entry(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.parentEntry = null;
        this.childEntries = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.metadata = new MetaData();
        this.permissionManager = new EntryPermissionManager();

        // Add current user as EDITOR
        this.getPermissionManager().addUserPermission(
                new UserPermission(author, EPermission.EDITOR)
        );

    }
    
    /**
     * Constructor with ID for loading from database
     */
    public Entry(int id, String title, String content, User author) {
        this(title, content, author);
        this.id = id;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Entry getParentEntry() {
        return parentEntry;
    }

    public ArrayList<Entry> getChildEntries() {
        return childEntries;
    }

    public int getChannelId() {
        return channelId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public User getAuthor() {
        return author;
    }

    public EntryPermissionManager getPermissionManager() {
        return permissionManager;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
        this.metadata.setLastModified(new Date());
    }

    public void setContent(String content) {
        this.content = content;
        this.metadata.setLastModified(new Date());
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChildEntries(ArrayList<Entry> childEntries) {
        this.childEntries = childEntries;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public void setPermissionManager(EntryPermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    // Entry relationship management
    /**
     * Sets the parent entry and adds this entry as a child to the parent
     */
    public void setParentEntry(Entry parent) throws CircularDependencyException {
        if (parent != null && isAncestorOf(parent)) {
            throw new CircularDependencyException("Cannot set parent: circular dependency detected");
        }
        this.parentEntry = parent;
        if (parent != null && !parent.childEntries.contains(this)) {
            parent.childEntries.add(this);
        }
        this.metadata.setLastModified(new Date());
    }

    /**
     * Adds a child entry and sets this entry as its parent
     */
    public void addChildEntry(Entry child) throws CircularDependencyException {
        if (child != null && isAncestorOf(this)) {
            throw new CircularDependencyException("Cannot add child: circular dependency detected");
        }
        if (!childEntries.contains(child)) {
            childEntries.add(child);
        }
        if (child.parentEntry != this) {
            child.setParentEntry(this);
        }
        this.metadata.setLastModified(new Date());
    }

    /**
     * Removes a child entry
     */
    public void removeChildEntry(Entry child) {
        if (childEntries.contains(child)) {
            childEntries.remove(child);
            if (child.parentEntry == this) {
                child.parentEntry = null;
            }
        }
        this.metadata.setLastModified(new Date());
    }

    /**
     * Checks if this entry is an ancestor of the given entry (to prevent circular dependencies)
     */
    private boolean isAncestorOf(Entry entry) {
        Entry current = entry.parentEntry;
        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current.parentEntry;
        }
        return false;
    }

    // Message management (comments stored in unified channel)
    /**
     * Adds a message to this entry's cached message list.
     * Note: To persist, use ChannelDAO.saveMessage() with this entry's channelId.
     */
    public void addMessage(Message message) {
        messages.add(message);
        this.metadata.setLastModified(new Date());
    }

    /**
     * Removes a message from this entry's cached message list.
     * Note: To persist deletion, use ChannelDAO.deleteMessage().
     */
    public void removeMessage(Message message) {
        messages.remove(message);
        this.metadata.setLastModified(new Date());
    }

    // Navigation methods
    /**
     * Gets the root entry of the tree
     */
    public Entry getRootEntry() {
        Entry current = this;
        while (current.parentEntry != null) {
            current = current.parentEntry;
        }
        return current;
    }

    /**
     * Gets all descendants of this entry (flattened list)
     */
    public List<Entry> getDescendants() {
        List<Entry> descendants = new ArrayList<>();
        for (Entry child : childEntries) {
            descendants.add(child);
            descendants.addAll(child.getDescendants());
        }
        return descendants;
    }

    /**
     * Gets the effective permission for a user on this entry (with sparse inheritance).
     * Walks up the tree to find the first explicit permission definition, then returns it.
     * This respects the sparse inheritance model: only stored permissions that differ from parent.
     * Important: If a permission is explicitly denied (removed) at this node, it stops cascading.
     * @param user The user to check permissions for
     * @return UserPermission if found anywhere in the tree, null if no permission found or explicitly denied
     */
    public UserPermission getUserPermissionWithCascade(User user) {
        // Check if this node has an explicit permission (including explicit denial by removal in sparse mode)
        boolean hasExplicit = this.permissionManager.hasExplicitPermission(user);
        
        if (hasExplicit) {
            // This node has an explicit permission, use it (could be null if sparse removal happened)
            return this.permissionManager.getUserPermission(user);
        }
        
        // No explicit permission at this node, check parent (sparse inheritance)
        if (this.parentEntry != null) {
            return this.parentEntry.getUserPermissionWithCascade(user);
        }
        
        // No permission found anywhere in the tree
        return null;
    }

    /**
     * Gets the effective permission level for a user considering sparse inheritance.
     * This represents what the user actually has access to.
     * @param user The user to check permissions for
     * @return EPermission the effective permission level, or null if no permission
     */
    public EPermission getEffectivePermission(User user) {
        UserPermission perm = getUserPermissionWithCascade(user);
        return perm != null ? perm.getPermission() : null;
    }

    /**
     * Gets the permission defined explicitly at this exact node (not inherited).
     * Used to check if this entry has an override.
     * @param user The user to check
     * @return UserPermission if explicitly defined on this node, null otherwise
     */
    public UserPermission getDirectPermission(User user) {
        return this.permissionManager.getUserPermission(user);
    }

    /**
     * Adds a user permission using sparse inheritance.
     * Only stores if the permission differs from what the user would inherit from parent.
     * Avoids storing redundant permissions.
     * @param user The user to add permission for
     * @param permission The permission level to grant
     * @throws CircularDependencyException if parent relationships are invalid
     */
    public void addUserPermissionSparse(User user, EPermission permission) throws CircularDependencyException {
        // Get what the user would inherit from parent
        EPermission inheritedPermission = null;
        if (this.parentEntry != null) {
            inheritedPermission = this.parentEntry.getEffectivePermission(user);
        }
        
        // Only store if different from inherited (sparse storage)
        if (inheritedPermission != permission) {
            this.permissionManager.addUserPermission(new UserPermission(user, permission));
        }
    }

    /**
     * Removes a user permission from this specific node.
     * The user will then inherit from parent (if available).
     * @param user The user to remove permission for
     */
    public void removeUserPermissionSparse(User user) {
        this.permissionManager.removeUserPermission(user);
    }

    /**
     * Gets all users with permissions defined on this specific entry (not cascaded).
     * Used for UI display and sparse inheritance checking.
     * @return List of UserPermissions explicitly defined on this entry
     */
    public List<UserPermission> getUsersWithPermissions() {
        return this.permissionManager.getUserPermissions();
    }

    /**
     * Checks if a user has access to this entry using "deny by default" logic.
     * 
     * Access is ONLY granted if:
     * 1. User has a resolved permission (cascaded from parent if needed)
     * 2. AND that permission is not null (explicit denial)
     * 3. AND the permission allows viewing (canView())
     * 
     * Access is DENIED if:
     * 1. No permission found in tree (null UserPermission)
     * 2. OR permission exists but is null (explicit denial marker)
     * 3. OR permission doesn't allow viewing
     * 
     * @param user The user to check access for
     * @return true if access is allowed, false otherwise
     */
    public boolean canUserAccess(User user) {
        // Get the effective permission with cascading
        UserPermission userPerm = getUserPermissionWithCascade(user);
        
        // Deny by default: if no permission record found anywhere in tree
        if (userPerm == null) {
            return false;
        }
        
        // Deny if permission exists but is null (explicit denial marker)
        if (userPerm.getPermission() == null) {
            return false;
        }
        
        // Allow only if permission explicitly allows viewing
        return userPerm.getPermission().canView();
    }

    // Exception class for circular dependencies
    public static class CircularDependencyException extends Exception {
        public CircularDependencyException(String message) {
            super(message);
        }
    }
}
