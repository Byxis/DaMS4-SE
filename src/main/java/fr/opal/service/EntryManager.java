package fr.opal.service;

import fr.opal.dao.EntryDAO;
import fr.opal.factory.AbstractDAOFactory;
import fr.opal.type.Comment;
import fr.opal.type.EPermission;
import fr.opal.type.Entry;
import fr.opal.type.EntryContextDTO;
import fr.opal.type.User;
import fr.opal.type.UserPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry Manager Service
 * Contains entry-related business logic and persistence coordination
 * Owns the complexity: CRUD, relationships, permissions, placeholder creation
 * Delegates persistence to DAO
 */
public class EntryManager
{
    private Entry currentEntry;
    private User currentUser;
    private AuthManager authManager;
    private EntryDAO dao;

    /**
     * Constructor with no parameters
     * Manager doesn't maintain current user - can handle any user
     */
    public EntryManager()
    {
        this.authManager = AuthManager.getInstance();
        this.dao = AbstractDAOFactory.getFactory().createEntryDAO();
        this.currentEntry = null;
        this.currentUser = null;
    }

    /**
     * Constructor with current user (for backward compatibility)
     */
    public EntryManager(User currentUser)
    {
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
    public EntryContextDTO getEntry(int id)
    {
        // Load the target entry with full details and Depth-1 neighbors
        Entry targetEntry = dao.loadEntryWithDetails(id);

        if (targetEntry == null)
        {
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
    public void persistEntry(Entry entry)
    {
        if (entry.getId() == 0)
        {
            // New entry - insert and update ID
            int id = dao.createEntry(entry);
            entry.setId(id);
        }
        else
        {
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
    public void persistEntryStructure(Entry rootEntry)
    {
        persistEntry(rootEntry);
        for (Entry child : rootEntry.getChildEntries())
        {
            persistEntryStructure(child);
        }
    }

    /**
     * Creates a new entry and saves it immediately
     */
    public Entry createNewEntry(String title, String content, User author)
    {
        Entry entry = fr.opal.factory.AbstractDAOFactory.getFactory().createEntry(title, content, author);
        persistEntry(entry);
        return entry;
    }

    /**
     * Removes an entry from the database
     */
    public void removeEntry(int id)
    {
        dao.deleteEntry(id);
    }

    /**
     * Gets all root entries from the database
     */
    public List<Entry> getAllRootEntries()
    {
        return dao.getRootEntries();
    }

    /**
     * Gets child entries of a parent from the database
     */
    public List<Entry> getChildrenOfEntry(int parentId)
    {
        return dao.getChildEntries(parentId);
    }

    // Navigation Methods

    /**
     * Gets the root of the entry tree
     */
    public Entry getRootEntry()
    {
        if (currentEntry == null)
            return null;
        return currentEntry.getRootEntry();
    }

    /**
     * Gets all root entries (project roots)
     */
    public List<Entry> getRootEntries()
    {
        return getAllRootEntries();
    }

    /**
     * Navigates to parent entry
     * Reloads parent from database with full Depth-1 context
     */
    public EntryContextDTO navigateToParent() throws PermissionException
    {
        if (currentEntry == null || currentEntry.getParentEntry() == null)
        {
            return null;
        }

        Entry parentMetadata = currentEntry.getParentEntry();
        if (!hasPermission(parentMetadata, EPermission.READER))
        {
            throw new PermissionException("User does not have permission to view parent entry");
        }

        // Reload parent with full Depth-1 context (its own parent, children, etc.)
        return getEntry(parentMetadata.getId());
    }

    /**
     * Navigates to a child entry
     * Reloads child from database with full Depth-1 context
     */
    public EntryContextDTO navigateToChild(Entry child) throws PermissionException
    {
        if (!hasPermission(child, EPermission.READER))
        {
            throw new PermissionException("User does not have permission to view this entry");
        }

        // Reload child with full Depth-1 context (its own parent, children, etc.)
        return getEntry(child.getId());
    }

    /**
     * Gets all child entries of the current entry
     */
    public List<Entry> getChildEntries()
    {
        if (currentEntry == null)
            return new ArrayList<>();
        return currentEntry.getChildEntries();
    }

    /**
     * Gets all descendants (flattened tree) of the current entry
     */
    public List<Entry> getDescendants()
    {
        if (currentEntry == null)
            return new ArrayList<>();
        return currentEntry.getDescendants();
    }

    // Entry Relationship Management

    /**
     * Changes the parent of an entry (with circular dependency check)
     */
    public void updateEntryParent(Entry entry, Entry newParent) throws Entry.CircularDependencyException
    {
        entry.setParentEntry(newParent);
        persistEntry(entry);
        if (newParent != null)
        {
            persistEntry(newParent);
        }
    }

    /**
     * Adds a child to an entry
     */
    public void attachChildToParent(Entry parent, Entry child) throws Entry.CircularDependencyException
    {
        parent.addChildEntry(child);
        persistEntry(parent);
        persistEntry(child);
    }

    /**
     * Removes a child from an entry
     */
    public void detachChildFromParent(Entry parent, Entry child)
    {
        parent.removeChildEntry(child);
        persistEntry(parent);
        persistEntry(child);
    }

    // Comment Management

    /**
     * Adds a comment to an entry with permission checks
     * Permission: COMMENTOR or EDITOR
     * Only the comment is persisted (Auto-Save)
     */
    public void addComment(int entryId, Comment comment) throws PermissionException
    {
        // SECURITY: Verify user has permission to comment
        Entry entry = dao.loadEntryWithDetails(entryId);
        if (entry == null)
        {
            throw new PermissionException("Entry not found");
        }

        if (!hasPermission(entry, EPermission.COMMENTOR))
        {
            throw new PermissionException("You do not have permission to comment on this entry");
        }

        // Add comment and persist only the comment
        entry.addComment(comment);
        dao.saveEntry(entry);
    }

    /**
     * Removes a comment from entry with permission checks
     * Permission: EDITOR (or comment author)
     */
    public void deleteCommentFromEntry(Entry entry, Comment comment) throws PermissionException
    {
        // SECURITY: Only EDITOR can remove comments
        if (!hasPermission(entry, EPermission.EDITOR))
        {
            throw new PermissionException("You do not have permission to delete comments");
        }

        entry.removeComment(comment);
        persistEntry(entry);
    }

    /**
     * Gets all comments for an entry
     */
    public List<Comment> getComments(Entry entry)
    {
        return entry.getComments();
    }

    // Entry Data & Permission Updates

    /**
     * Updates entry data and permissions (Manual Save)
     * Scope: Title, Content, and Permissions
     * Permission: EDITOR only
     * SECURITY: READER and COMMENTOR users are strictly blocked
     */
    public void updateEntry(int entryId, String newTitle, String newContent, List<UserPermission> permissionOverrides)
        throws PermissionException
    {
        // SECURITY: Only EDITOR can modify entry data and permissions
        Entry entry = dao.loadEntryWithDetails(entryId);
        if (entry == null)
        {
            throw new PermissionException("Entry not found");
        }

        // STRICT CHECK: READER users are absolutely blocked
        UserPermission userPerm = entry.getPermissionManager().getUserPermission(currentUser);
        if (userPerm != null && userPerm.getPermission() == EPermission.READER)
        {
            throw new PermissionException("Read-only users cannot modify entries");
        }

        // STRICT CHECK: Only EDITOR can proceed
        if (!hasPermission(entry, EPermission.EDITOR))
        {
            throw new PermissionException("You do not have editor permissions for this entry");
        }

        // Update entry data
        entry.setTitle(newTitle);
        entry.setContent(newContent);

        // Update permissions if provided
        if (permissionOverrides != null && !permissionOverrides.isEmpty())
        {
            for (UserPermission perm : permissionOverrides)
            {
                entry.getPermissionManager().addUserPermission(perm);
            }
        }

        // Persist the complete entry with all changes
        persistEntry(entry);
    }

    // Permission Management

    /**
     * Sets a user's permission on an entry by User object
     */
    public void setUserPermission(Entry entry, User user, EPermission permission)
    {
        UserPermission up = new UserPermission(user, permission);
        entry.getPermissionManager().addUserPermission(up);
        persistEntry(entry);
    }

    /**
     * Sets a user's permission on an entry by username
     * Looks up the user by username and applies the permission
     */
    public void setUserPermissionByUsername(Entry entry, String username, EPermission permission) throws Exception
    {
        if (username == null || username.trim().isEmpty())
        {
            throw new Exception("Username cannot be empty");
        }
        if (permission == null)
        {
            throw new Exception("Permission cannot be null");
        }

        // Look up user by username
        User user = authManager.getUserByUsername(username);
        if (user == null)
        {
            throw new Exception("User not found: " + username);
        }

        // Set the permission
        setUserPermission(entry, user, permission);
    }

    /**
     * Checks if current user has permission on specific entry
     */
    private boolean hasPermission(Entry entry, EPermission requiredPermission)
    {
        if (entry == null || requiredPermission == null)
        {
            return false;
        }

        UserPermission userPerm = entry.getPermissionManager().getUserPermission(currentUser);
        if (userPerm == null)
        {
            return false;
        }

        EPermission userPermission = userPerm.getPermission();

        // Check if user permission meets or exceeds required permission
        if (requiredPermission == EPermission.READER)
        {
            return userPermission.canView();
        }
        else if (requiredPermission == EPermission.COMMENTOR)
        {
            return userPermission.canComment();
        }
        else if (requiredPermission == EPermission.EDITOR)
        {
            return userPermission.canEdit();
        }

        return false;
    }

    // Placeholder Entry Management

    /**
     * Initializes a complete placeholder entry structure with all test variations
     * Creates the structure, persists it to database, and returns it
     * @param ownerUsername The username of the entry owner (e.g., "lez")
     * @return The root placeholder entry with full hierarchy
     */
    public Entry initializePlaceholderStructure(String ownerUsername) throws Exception
    {
        // Load owner user
        User owner = authManager.getUserByUsername(ownerUsername);
        if (owner == null)
        {
            throw new RuntimeException("Owner user '" + ownerUsername + "' not found");
        }

        // Build the structure (pure business logic)
        Entry placeholderEntry = buildPlaceholderStructure(owner);

        // Persist the entire structure to database
        persistEntryStructure(placeholderEntry);

        return placeholderEntry;
    }

    /**
     * Ensures the placeholder entry structure exists in the database
     * Implements idempotent initialization: load from DB if exists, create if not
     * @param ownerUsername The username of the entry owner (e.g., "lez")
     * @return The root entry ID if created/loaded successfully, 0 if operation failed
     */
    public int ensurePlaceholderStructureExists(String ownerUsername) throws Exception
    {
        // Load owner user
        User owner = authManager.getUserByUsername(ownerUsername);
        if (owner == null)
        {
            throw new RuntimeException("Owner user '" + ownerUsername + "' not found");
        }

        // Try to load existing root entry from database
        List<Entry> rootEntries = getAllRootEntries();
        for (Entry entry : rootEntries)
        {
            if ("Sample Project".equals(entry.getTitle()))
            {
                // Root entry already exists in database - idempotency achieved
                return entry.getId();
            }
        }

        // Root entry doesn't exist - create it
        Entry placeholderEntry = buildPlaceholderStructure(owner);
        persistEntryStructure(placeholderEntry);

        // Return the ID of the created root entry
        return placeholderEntry.getId();
    }

    /**
     * Fetches the root placeholder entry from the database with Depth-1 context
     * Always queries DB, never uses in-memory cache
     * @return EntryContextDTO with root entry and its children, null if not found
     */
    public EntryContextDTO fetchPlaceholderRootFromDatabase()
    {
        List<Entry> rootEntries = getAllRootEntries();
        for (Entry entry : rootEntries)
        {
            if ("Sample Project".equals(entry.getTitle()))
            {
                // Load with full Depth-1 context
                return getEntry(entry.getId());
            }
        }
        return null;
    }

    /**
     * Builds a complete placeholder entry structure with all test variations
     * This is PURE business logic - builds the structure but does NOT persist
     * @param owner The owner User object for all entries
     * @return The root placeholder entry with full hierarchy
     */
    public Entry buildPlaceholderStructure(User owner) throws Exception
    {
        // Create root entry
        Entry placeholderEntry =
            new Entry("Sample Project", "This is a sample project for testing the Entry Management System.", owner);

        // Create nested child entries
        Entry chapter1 = new Entry("Chapter 1: Introduction", "Introduction to the project and its purpose.", owner);
        placeholderEntry.addChildEntry(chapter1);

        Entry section1_1 =
            new Entry("1.1 Overview", "This section provides an overview of the system architecture.", owner);
        chapter1.addChildEntry(section1_1);

        Entry section1_2 = new Entry("1.2 Key Features", "Key features and capabilities of the system.", owner);
        chapter1.addChildEntry(section1_2);

        // Entry with NO permissions override
        Entry chapter2 = new Entry(
            "[NO ACCESS] Chapter 2: Implementation", "Implementation details and technical decisions.", owner);
        chapter2.removeUserPermissionSparse(owner); // Explicitly deny
        placeholderEntry.addChildEntry(chapter2);

        // Entry with READER only permission override
        Entry section2_1 = new Entry("[READER ONLY] Architecture", "System architecture and design patterns.", owner);
        section2_1.removeUserPermissionSparse(owner); // Remove inherited EDITOR
        section2_1.addUserPermissionSparse(owner, EPermission.READER); // Override to READER
        placeholderEntry.addChildEntry(section2_1);

        // Entry with COMMENTOR only permission override
        Entry section2_2 = new Entry("[COMMENTER ONLY] Database Schema", "Database design and relationships.", owner);
        section2_2.removeUserPermissionSparse(owner); // Remove inherited EDITOR
        section2_2.addUserPermissionSparse(owner, EPermission.COMMENTOR); // Override to COMMENTOR
        placeholderEntry.addChildEntry(section2_2);

        Entry chapter3 = new Entry("Chapter 3: Conclusion", "Final thoughts and future improvements.", owner);
        placeholderEntry.addChildEntry(chapter3);

        return placeholderEntry;
    }

    /**
     * Gets the current entry
     */
    public Entry getCurrentEntry()
    {
        return currentEntry;
    }

    /**
     * Sets the current entry
     */
    public void setCurrentEntry(Entry entry)
    {
        this.currentEntry = entry;
    }

    /**
     * Exception for permission-related errors
     */
    public static class PermissionException extends Exception
    {
        public PermissionException(String message)
        {
            super(message);
        }
    }
}
