package fr.opal.dao;

import fr.opal.type.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of EntryDAO
 */
public class MySQLEntryDAO implements EntryDAO {
    private Connection conn;
    private MySQLUserDAO userDAO;

    /**
     * Constructor with database connection (must be managed by factory)
     */
    public MySQLEntryDAO(Connection conn) {
        this.conn = conn;
        this.userDAO = new MySQLUserDAO(conn);
    }

    /**
     * Retrieves an entry by its ID with ALL data (eager load)
     * Child entries are loaded with basic data only (lazy load - title, id, permissions)
     */
    @Override
    public Entry getEntryById(int id) {
        Entry entry = loadEntryBasicData(id);
        if (entry != null) {
            // Eagerly load all data for this entry
            entry.setComments(loadComments(id));
            entry.setPermissionManager(loadPermissions(id));
            
            // Lazy load child entries (only title, id, and permissions)
            List<Entry> children = getChildEntries(id);
            for (Entry child : children) {
                child.setPermissionManager(loadPermissions(child.getId()));
            }
            entry.setChildEntries((ArrayList<Entry>) children);
        }
        return entry;
    }

    /**
     * Saves an entry to the database (updates existing)
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
            
            // Update comments
            saveComments(entry);
            
            // Update permissions
            savePermissions(entry);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new entry in the database
     */
    @Override
    public int createEntry(Entry entry) {
        String sql = "INSERT INTO entries(title, content, parent_id, author_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entry.getTitle());
            ps.setString(2, entry.getContent());
            
            if (entry.getParentEntry() != null) {
                ps.setInt(3, entry.getParentEntry().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setInt(4, entry.getAuthor().getId());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    entry.setId(id);
                    
                    // Save comments if any
                    saveComments(entry);
                    
                    // Save permissions if any
                    savePermissions(entry);
                    
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all root entries (entries without parents)
     */
    @Override
    public List<Entry> getRootEntries() {
        List<Entry> entries = new ArrayList<>();
        String sql = "SELECT id, title, content, parent_id, author_id, creation_date, last_modified " +
                     "FROM entries WHERE parent_id IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entries.add(buildEntryFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Loads basic entry data from database
     */
    private Entry loadEntryBasicData(int id) {
        String sql = "SELECT id, title, content, parent_id, author_id, creation_date, last_modified " +
                     "FROM entries WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildEntryFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        
        // Load author
        int authorId = rs.getInt("author_id");
        User author = userDAO.getUserById(String.valueOf(authorId));
        entry.setAuthor(author);
        
        // Load metadata
        MetaData metadata = new MetaData();
        metadata.setCreationDate(rs.getTimestamp("creation_date"));
        metadata.setLastModified(rs.getTimestamp("last_modified"));
        entry.setMetadata(metadata);
        
        // Load parent entry if exists (only basic data to avoid deep recursion)
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            Entry parent = loadEntryBasicData(parentId);
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
     * Load comments for an entry
     */
    private List<Comment> loadComments(int entryId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, content, author_id, created_date FROM comments WHERE entry_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                int authorId = rs.getInt("author_id");
                User author = userDAO.getUserById(String.valueOf(authorId));
                java.util.Date createdDate = new java.util.Date(rs.getTimestamp("created_date").getTime());
                
                Comment comment = new Comment(id, content, author, createdDate);
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * Save comments for an entry
     */
    private void saveComments(Entry entry) {
        if (entry.getComments() == null) return;
        
        // Delete existing comments
        String deleteSql = "DELETE FROM comments WHERE entry_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, entry.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Insert new comments
        String insertSql = "INSERT INTO comments(entry_id, content, author_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (Comment comment : entry.getComments()) {
                ps.setInt(1, entry.getId());
                ps.setString(2, comment.getContent());
                ps.setInt(3, comment.getAuthor().getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load permissions for an entry
     */
    private EntryPermissionManager loadPermissions(int entryId) {
        EntryPermissionManager manager = new EntryPermissionManager();
        List<UserPermission> permissions = new ArrayList<>();
        
        String sql = "SELECT user_id, permission FROM entry_permissions WHERE entry_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                User user = userDAO.getUserById(String.valueOf(userId));
                
                if (user != null) {
                    String permStr = rs.getString("permission");
                    EPermission permission = EPermission.valueOf(permStr);
                    UserPermission userPerm = new UserPermission(user, permission);
                    permissions.add(userPerm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        
        // Insert new permissions
        String insertSql = "INSERT INTO entry_permissions(entry_id, user_id, permission) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (UserPermission userPerm : entry.getPermissionManager().getUserPermissions()) {
                ps.setInt(1, entry.getId());
                ps.setInt(2, userPerm.getUser().getId());
                ps.setString(3, userPerm.getPermission().name());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
