package fr.opal.service;

import fr.opal.type.Entry;
import fr.opal.type.User;
import fr.opal.type.Comment;
import fr.opal.type.UserPermission;
import fr.opal.type.EPermission;
import fr.opal.facade.EntryFacade;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry Manager Service
 * Manages entry-related business logic including CRUD operations, navigation, and import/export
 */
public class EntryManager {

    private EntryFacade facade;
    private Entry currentEntry;
    private User currentUser;
    private AuthManager authManager;

    /**
     * Constructor
     */
    public EntryManager(User currentUser) {
        this.facade = EntryFacade.getInstance();
        this.currentUser = currentUser;
        this.authManager = AuthManager.getInstance();
        this.currentEntry = null;
    }

    // Entry Management Methods

    /**
     * Loads an entry by ID
     */
    public Entry loadEntry(int id) {
        currentEntry = facade.loadEntry(id);
        return currentEntry;
    }

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
     * Creates a new entry
     */
    public Entry createEntry(String title, String content) throws PermissionException {
        if (!hasPermission(EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to create entries");
        }
        Entry entry = facade.createEntry(title, content, currentUser);
        return entry;
    }

    /**
     * Saves an entry
     */
    public void saveEntry(Entry entry) throws PermissionException {
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to edit this entry");
        }
        facade.saveEntry(entry);
    }

    /**
     * Deletes an entry
     */
    public void deleteEntry(Entry entry) throws PermissionException {
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to delete this entry");
        }
        facade.deleteEntry(entry.getId());
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
        return facade.getRootEntries();
    }

    /**
     * Navigates to parent entry
     */
    public Entry navigateToParent() throws PermissionException {
        if (currentEntry == null || currentEntry.getParentEntry() == null) {
            return null;
        }
        
        Entry parent = currentEntry.getParentEntry();
        if (!hasPermission(parent, EPermission.READER)) {
            throw new PermissionException("User does not have permission to view parent entry");
        }
        
        currentEntry = parent;
        return currentEntry;
    }

    /**
     * Navigates to a child entry
     */
    public Entry navigateToChild(Entry child) throws PermissionException {
        if (!hasPermission(child, EPermission.READER)) {
            throw new PermissionException("User does not have permission to view this entry");
        }
        currentEntry = child;
        return currentEntry;
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
    public void changeParent(Entry entry, Entry newParent) throws PermissionException, Entry.CircularDependencyException {
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to modify this entry");
        }
        facade.updateParentEntry(entry, newParent);
    }

    /**
     * Adds a child to an entry
     */
    public void addChild(Entry parent, Entry child) throws PermissionException, Entry.CircularDependencyException {
        if (!hasPermission(parent, EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to modify this entry");
        }
        facade.addChildEntry(parent, child);
    }

    /**
     * Removes a child from an entry
     */
    public void removeChild(Entry parent, Entry child) throws PermissionException {
        if (!hasPermission(parent, EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to modify this entry");
        }
        facade.removeChildEntry(parent, child);
    }

    // Comment Management

    /**
     * Adds a comment to an entry
     */
    public void addComment(Entry entry, String commentText) throws PermissionException {
        if (!hasPermission(entry, EPermission.COMMENTOR)) {
            throw new PermissionException("User does not have permission to comment on this entry");
        }
        Comment comment = new Comment(commentText, currentUser);
        facade.addComment(entry, comment);
    }

    /**
     * Removes a comment
     */
    public void removeComment(Entry entry, Comment comment) throws PermissionException {
        if (!hasPermission(entry, EPermission.EDITOR) && comment.getAuthor().getId() != currentUser.getId()) {
            throw new PermissionException("User does not have permission to remove this comment");
        }
        facade.removeComment(entry, comment);
    }

    /**
     * Gets all comments for an entry
     */
    public List<Comment> getComments(Entry entry) {
        return entry.getComments();
    }

    // Permission Management

    /**
     * Sets a user's permission on an entry
     */
    public void setUserPermission(Entry entry, User user, EPermission permission) throws PermissionException {
        if (!hasPermission(entry, EPermission.EDITOR)) {
            throw new PermissionException("User does not have permission to modify entry permissions");
        }
        UserPermission up = new UserPermission(user, permission);
        entry.getPermissionManager().addUserPermission(up);
        facade.saveEntry(entry);
    }

    /**
     * Checks if current user has permission on specific entry
     */
    private boolean hasPermission(Entry entry, EPermission requiredPermission) {
        if (entry == null || requiredPermission == null) {
            return false;
        }
        
        UserPermission userPerm = entry.getPermissionManager().getUserPermission(currentUser);
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

    /**
     * Checks if current user has a global permission type (based on user role)
     */
    private boolean hasPermission(EPermission permissionType) {
        // TODO: Implement proper permission checking based on user roles
        // For now, grant all permissions to authenticated users
        return authManager.isAuthenticated();
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

