package fr.opal.type;

import java.util.List;

/**
 * Data Transfer Object for entry navigation context
 * Implements the Depth-1 Radial Lookahead strategy
 * 
 * Contains:
 * - Target Entry: Full details (content, permissions, comments)
 * - Parent Entry: Metadata only (ID, Title, Permissions) - null if root
 * - Children Entries: List of metadata only (ID, Title, Permissions)
 */
public class EntryContextDTO {
    
    private Entry targetEntry;
    private Entry parentEntry;
    private List<Entry> childEntries;
    
    /**
     * Constructor for the entry context
     */
    public EntryContextDTO(Entry targetEntry, Entry parentEntry, List<Entry> childEntries) {
        this.targetEntry = targetEntry;
        this.parentEntry = parentEntry;
        this.childEntries = childEntries;
    }
    
    /**
     * Gets the target entry (full details)
     */
    public Entry getTargetEntry() {
        return targetEntry;
    }
    
    /**
     * Gets the parent entry (metadata only) or null if root
     */
    public Entry getParentEntry() {
        return parentEntry;
    }
    
    /**
     * Gets the list of child entries (metadata only)
     */
    public List<Entry> getChildEntries() {
        return childEntries;
    }
    
    /**
     * Checks if this is a root entry (no parent)
     */
    public boolean isRoot() {
        return parentEntry == null;
    }
    
    /**
     * Checks if this entry has children
     */
    public boolean hasChildren() {
        return childEntries != null && !childEntries.isEmpty();
    }
    
    /**
     * Gets the number of children
     */
    public int getChildCount() {
        return childEntries != null ? childEntries.size() : 0;
    }
}
