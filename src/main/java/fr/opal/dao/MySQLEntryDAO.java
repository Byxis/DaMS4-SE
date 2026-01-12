package fr.opal.dao;

import fr.opal.exception.DataAccessException;
import fr.opal.type.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of EntryDAO
 * Uses unified channel architecture for comments (messages).
 */
public class MySQLEntryDAO extends EntryDAO {
    private Connection conn;
    private MySQLUserDAO userDAO;
    private MySQLChannelDAO channelDAO;

    /**
     * Constructor with database connection (must be managed by factory)
     */
    public MySQLEntryDAO(Connection conn) {
        this.conn = conn;
        this.userDAO = new MySQLUserDAO(conn);
        this.channelDAO = new MySQLChannelDAO(conn);
    }

    /**
     * Retrieves an entry by its ID with ALL data (eager load)
     * Implements Depth-1 Radial strategy: current entry (full), parent (metadata+permissions), children (metadata+permissions)
     */
    @Override
    public Entry getEntryById(int id) {
        Entry entry = loadEntryBasicData(id);
        if (entry != null) {
            // Eagerly load messages from unified channel
            if (entry.getChannelId() > 0) {
                entry.setMessages(channelDAO.getMessagesForChannel(entry.getChannelId()));
            }
            entry.setPermissionManager(loadPermissions(id));
            
            // Load parent entry with permissions (Depth-1 Upward)
            if (entry.getParentEntry() != null) {
                Entry parent = entry.getParentEntry();
                parent.setPermissionManager(loadPermissions(parent.getId()));
                try {
                    entry.setParentEntry(parent);
                } catch (Entry.CircularDependencyException e) {
                    // This shouldn't happen when loading from database
                    e.printStackTrace();
                }
            }
            
            // Lazy load child entries with permissions (Depth-1 Downward)
            List<Entry> children = getChildEntries(id);
            for (Entry child : children) {
                child.setPermissionManager(loadPermissions(child.getId()));
                // IMPORTANT: Set parent reference for permission cascading
                try {
                    child.setParentEntry(entry);
                } catch (Entry.CircularDependencyException e) {
                    // This shouldn't happen when loading from database
                    e.printStackTrace();
                }
            }
            entry.setChildEntries((ArrayList<Entry>) children);
        }
        return entry;
    }

    /**
     * Saves an entry to the database (updates existing)
     * Note: Messages are managed separately via ChannelDAO
     */
    @Override
    public void saveEntry(Entry entry) {
        String sql = "UPDATE entries SET title = ?, content = ?, parent_id = ?, " +
                     "last_modified = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getTitle());
            ps.setString(2, entry.getContent());
            
            if (entry.getParentEntry() != null) {
                ps.setInt(3, entry.getParentEntry().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setInt(4, entry.getId());
            ps.executeUpdate();
            
            // Update permissions
            savePermissions(entry);
            
        } catch (SQLException e) {
            throw new DataAccessException("Error saving entry: " + entry.getId(), e);
        }
    }

    /**
     * Creates a new entry in the database with its own channel for comments.
     */
    @Override
    public int createEntry(Entry entry) {
        // First create a channel for this entry's comments
        int channelId = channelDAO.createChannel();
        entry.setChannelId(channelId);
        
        String sql = "INSERT INTO entries(title, content, parent_id, author_id, channel_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entry.getTitle());
            ps.setString(2, entry.getContent());
            
            if (entry.getParentEntry() != null) {
                ps.setInt(3, entry.getParentEntry().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            // Ensure author exists and has valid ID
            if (entry.getAuthor() == null || entry.getAuthor().getId() == 0) {
                throw new DataAccessException("Entry author is missing or has invalid ID", null);
            }
            ps.setInt(4, entry.getAuthor().getId());
            ps.setInt(5, channelId);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    entry.setId(id);
                    
                    // Save permissions if any
                    savePermissions(entry);
                    
                    return id;
                }
            }
        } catch (SQLException e) {
            // Clean up channel if entry creation fails
            channelDAO.deleteChannel(channelId);
            throw new DataAccessException("Error creating entry: " + entry.getTitle(), e);
        }
        return 0;
    }

    /**
     * Deletes an entry from the database
     */
    @Override
    public void deleteEntry(int id) {
        String sql = "DELETE FROM entries WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting entry: " + id, e);
        }
    }

    /**
     * Retrieves all root entries (entries without parents)
     */
    @Override
    public List<Entry> getRootEntries() {
        List<Entry> entries = new ArrayList<>();
        String sql = "SELECT id, title, content, parent_id, author_id, channel_id, creation_date, last_modified " +
                     "FROM entries WHERE parent_id IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entries.add(buildEntryFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving root entries", e);
        }
        return entries;
    }

    /**
     * Retrieves all child entries of a given parent (lazy load - only title, id)
     */
    @Override
    public List<Entry> getChildEntries(int parentId) {
        List<Entry> entries = new ArrayList<>();
        String sql = "SELECT id, title FROM entries WHERE parent_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Only load title and id
                Entry entry = buildEntryMinimalFromResultSet(rs);
                entries.add(entry);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving child entries for parent: " + parentId, e);
        }
        return entries;
    }

    /**
     * Loads basic entry data from database
     */
    private Entry loadEntryBasicData(int id) {
        String sql = "SELECT id, title, content, parent_id, author_id, channel_id, creation_date, last_modified " +
                     "FROM entries WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildEntryFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error loading entry: " + id, e);
        }
        return null;
    }

    /**
     * Helper method to build full Entry object from ResultSet
     */
    private Entry buildEntryFromResultSet(ResultSet rs) throws SQLException {
        Entry entry = new Entry();
        entry.setId(rs.getInt("id"));
        entry.setTitle(rs.getString("title"));
        entry.setContent(rs.getString("content"));
        
        // Load channel ID for unified messaging
        int channelId = rs.getInt("channel_id");
        if (!rs.wasNull()) {
            entry.setChannelId(channelId);
        }
        
        // Load author using database ID (not username)
        int authorId = rs.getInt("author_id");
        User author = userDAO.getUserByDatabaseId(authorId);
        entry.setAuthor(author);
        
        // Load metadata
        MetaData metadata = new MetaData();
        metadata.setCreationDate(rs.getTimestamp("creation_date"));
        metadata.setLastModified(rs.getTimestamp("last_modified"));
        entry.setMetadata(metadata);
        
        // Load parent entry if exists (recursively load full parent chain for permission cascading)
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            Entry parent = loadParentChainWithPermissions(parentId);
            try {
                entry.setParentEntry(parent);
            } catch (Entry.CircularDependencyException e) {
                // This shouldn't happen when loading from database
                e.printStackTrace();
            }
        }
        
        return entry;
    }

    /**
     * Recursively loads parent entry with permissions, stopping permission loading at the first entry that has permissions.
     * However, parent references are ALWAYS set for navigation purposes.
     * This implements "permission boundary" - the first ancestor with permissions becomes the source of truth.
     */
    private Entry loadParentChainWithPermissions(int parentId) {
        String sql = "SELECT id, title, content, parent_id, author_id, channel_id, creation_date, last_modified " +
                     "FROM entries WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Entry parent = new Entry();
                parent.setId(rs.getInt("id"));
                parent.setTitle(rs.getString("title"));
                parent.setContent(rs.getString("content"));
                
                // Load channel ID
                int channelId = rs.getInt("channel_id");
                if (!rs.wasNull()) {
                    parent.setChannelId(channelId);
                }
                
                // Load author
                int authorId = rs.getInt("author_id");
                User author = userDAO.getUserByDatabaseId(authorId);
                parent.setAuthor(author);
                
                // Load permissions for this parent
                EntryPermissionManager permissions = loadPermissions(parentId);
                parent.setPermissionManager(permissions);
                
                // ALWAYS load the parent reference for navigation, but only continue loading
                // permissions up the chain if this entry has NO permissions (permission boundary).
                int grandparentId = rs.getInt("parent_id");
                if (!rs.wasNull()) {
                    if (!permissions.hasAnyPermissions()) {
                        // No permissions here, continue loading full chain with permissions
                        Entry grandparent = loadParentChainWithPermissions(grandparentId);
                        try {
                            parent.setParentEntry(grandparent);
                        } catch (Entry.CircularDependencyException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Permission boundary reached - still load parent for navigation but minimal
                        Entry grandparent = loadParentMinimal(grandparentId);
                        try {
                            parent.setParentEntry(grandparent);
                        } catch (Entry.CircularDependencyException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                return parent;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error loading parent chain: " + parentId, e);
        }
        return null;
    }

    /**
     * Loads minimal parent entry (just id and title) for navigation purposes only.
     * Used when we've already hit a permission boundary but still need parent references.
     */
    private Entry loadParentMinimal(int parentId) {
        String sql = "SELECT id, title, parent_id FROM entries WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Entry parent = new Entry();
                parent.setId(rs.getInt("id"));
                parent.setTitle(rs.getString("title"));
                
                // Continue loading minimal parent chain for navigation
                int grandparentId = rs.getInt("parent_id");
                if (!rs.wasNull()) {
                    Entry grandparent = loadParentMinimal(grandparentId);
                    try {
                        parent.setParentEntry(grandparent);
                    } catch (Entry.CircularDependencyException e) {
                        e.printStackTrace();
                    }
                }
                
                return parent;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error loading minimal parent: " + parentId, e);
        }
        return null;
    }

    /**
     * Helper method to build minimal Entry object from ResultSet (for lazy loading)
     * Only loads: id, title
     * Permissions are loaded separately in getEntryById()
     */
    private Entry buildEntryMinimalFromResultSet(ResultSet rs) throws SQLException {
        Entry entry = new Entry();
        entry.setId(rs.getInt("id"));
        entry.setTitle(rs.getString("title"));
        return entry;
    }

    /**
     * Load permissions for an entry using username (not user_id)
     */
    private EntryPermissionManager loadPermissions(int entryId) {
        EntryPermissionManager manager = new EntryPermissionManager();
        List<UserPermission> permissions = new ArrayList<>();
        
        String sql = "SELECT username, permission FROM entry_permissions WHERE entry_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                User user = userDAO.getUserById(username);
                
                if (user != null) {
                    String permStr = rs.getString("permission");
                    // Use NONE for explicitly denied permissions (sparse inheritance)
                    EPermission permission = (permStr != null) ? EPermission.valueOf(permStr) : EPermission.NONE;
                    UserPermission userPerm = new UserPermission(user, permission);
                    permissions.add(userPerm);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error loading permissions for entry: " + entryId, e);
        }
        
        manager.setUserPermissions(permissions);
        return manager;
    }

    /**
     * Save permissions for an entry
     */
    private void savePermissions(Entry entry) {
        if (entry.getPermissionManager() == null || 
            entry.getPermissionManager().getUserPermissions() == null) return;
        
        // Delete existing permissions
        String deleteSql = "DELETE FROM entry_permissions WHERE entry_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, entry.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting permissions for entry: " + entry.getId(), e);
        }
        
        // Insert new permissions using username instead of user_id
        String insertSql = "INSERT INTO entry_permissions(entry_id, username, permission) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (UserPermission userPerm : entry.getPermissionManager().getUserPermissions()) {
                ps.setInt(1, entry.getId());
                ps.setString(2, userPerm.getUser().getUsername());
                // Store NONE as NULL in database for sparse inheritance
                EPermission perm = userPerm.getPermission();
                if (perm != null && perm != EPermission.NONE) {
                    ps.setString(3, perm.name());
                } else {
                    ps.setNull(3, Types.VARCHAR);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving permissions for entry: " + entry.getId(), e);
        }
    }

    /**
     * Updates entry relationships in the database
     */
    @Override
    public void updateEntryRelationships(Entry entry) {
        String sql = "UPDATE entries SET parent_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (entry.getParentEntry() != null) {
                ps.setInt(1, entry.getParentEntry().getId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, entry.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating entry relationships: " + entry.getId(), e);
        }
    }

    /**
     * Loads all entry data including comments and metadata
     */
    @Override
    public Entry loadEntryWithDetails(int id) {
        return getEntryById(id);
    }
}
