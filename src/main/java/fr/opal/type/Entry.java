package fr.opal.type;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a node in the project's hierarchical tree.
 * An Entry may contain text, comments, and relationships to parent and child entries.
 */
public class Entry {
    private int id;
    private String title;
    private String content;
    private Entry parentEntry;
    private ArrayList<Entry> childEntries;
    private List<Comment> comments;
    private MetaData metadata;
    private User author;
    private EntryPermissionManager permissionManager;
    
    /**
     * Default constructor for database loading
     */
    public Entry() {
        this.childEntries = new ArrayList<>();
        this.comments = new ArrayList<>();
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
        this.comments = new ArrayList<>();
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

    public List<Comment> getComments() {
        return comments;
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

    public void setComments(List<Comment> comments) {
        this.comments = comments;
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

    // Comment management
    /**
     * Adds a comment to this entry
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        this.metadata.setLastModified(new Date());
    }

    /**
     * Removes a comment from this entry
     */
    public void removeComment(Comment comment) {
        comments.remove(comment);
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

    // Exception class for circular dependencies
    public static class CircularDependencyException extends Exception {
        public CircularDependencyException(String message) {
            super(message);
        }
    }
}
