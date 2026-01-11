package fr.opal.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Handles database schema initialization.
 */
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    /**
     * Ensures the database schema is up to date with the unified channel architecture.
     * Adds missing columns/tables without dropping existing data.
     * Safe to call on every startup.
     */
    public static void ensureSchemaUpToDate(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            LOGGER.info("Checking database schema for unified channel architecture...");

            // Ensure channels table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS channels (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;");

            // Ensure messages table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "channel_id INT NOT NULL," +
                    "sender_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_channel_created (channel_id, created_at DESC)" +
                    ") ENGINE=InnoDB;");

            // Check if friendships table has channel_id column
            try {
                ResultSet rs = stmt.executeQuery("SELECT channel_id FROM friendships LIMIT 1");
                rs.close();
                LOGGER.fine("friendships.channel_id column already exists");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                LOGGER.info("Adding channel_id column to friendships table...");
                stmt.execute("ALTER TABLE friendships ADD COLUMN channel_id INT UNIQUE NULL");
                LOGGER.info("Added channel_id column to friendships table");
            }

            // Ensure entries table has channel_id column
            try {
                ResultSet rs = stmt.executeQuery("SELECT channel_id FROM entries LIMIT 1");
                rs.close();
                LOGGER.fine("entries.channel_id column already exists");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                LOGGER.info("Adding channel_id column to entries table...");
                stmt.execute("ALTER TABLE entries ADD COLUMN channel_id INT UNIQUE NOT NULL");
                LOGGER.info("Added channel_id column to entries table");
            }

            // Create channels for accepted friendships that don't have one yet
            createMissingFriendshipChannels(connection);

            LOGGER.info("Database schema check complete");

        } catch (SQLException e) {
            LOGGER.severe("Error checking/updating schema: " + e.getMessage());
            throw new RuntimeException("Schema check failed", e);
        }
    }

    /**
     * Creates channels for any accepted friendships that are missing a channel_id.
     * This handles friendships that were accepted before the channel architecture was added.
     */
    private static void createMissingFriendshipChannels(Connection connection) {
        String findMissing = "SELECT id FROM friendships WHERE status = 'ACCEPTED' AND channel_id IS NULL";
        String createChannel = "INSERT INTO channels() VALUES()";
        String updateFriendship = "UPDATE friendships SET channel_id = ? WHERE id = ?";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(findMissing)) {
            
            int count = 0;
            while (rs.next()) {
                int friendshipId = rs.getInt("id");
                
                // Create a channel
                try (PreparedStatement createPs = connection.prepareStatement(createChannel, Statement.RETURN_GENERATED_KEYS)) {
                    createPs.executeUpdate();
                    try (ResultSet keyRs = createPs.getGeneratedKeys()) {
                        if (keyRs.next()) {
                            int channelId = keyRs.getInt(1);
                            
                            // Assign channel to friendship
                            try (PreparedStatement updatePs = connection.prepareStatement(updateFriendship)) {
                                updatePs.setInt(1, channelId);
                                updatePs.setInt(2, friendshipId);
                                updatePs.executeUpdate();
                                count++;
                            }
                        }
                    }
                }
            }
            
            if (count > 0) {
                LOGGER.info("Created channels for " + count + " existing friendships");
            }
            
        } catch (SQLException e) {
            LOGGER.warning("Error creating missing friendship channels: " + e.getMessage());
            // Don't fail startup for this - just log the warning
        }
    }


    /**
     * Creates a mock entry hierarchy for testing.
     * Structure:
     * - Sample Project (root entry)
     *   - Section 1
     *     - Subsection 1.1
     *     - Subsection 1.2
     *     - Subsection 1.3
     *     - Subsection 1.4
     * 
     * Root entry has EDITOR permission granted to user "lez".
     * 
     * @param connection Database connection
     * @param authorId ID of the user creating the entries
     */
    public static void createMockEntryHierarchy(Connection connection, int authorId) {
        try (Statement stmt = connection.createStatement()) {
            LOGGER.info("Creating mock entry hierarchy with 6 entries...");

            // Create 6 channels (one for each entry: root + section + 4 subsections)
            for (int i = 0; i < 6; i++) {
                stmt.execute("INSERT INTO channels() VALUES()");
            }

            // Get the starting channel ID
            ResultSet rs = stmt.executeQuery("SELECT MAX(id) - 5 as id FROM channels");
            rs.next();
            int startChannelId = rs.getInt("id");
            rs.close();

            // Insert root entry
            String rootEntrySql = "INSERT INTO entries(title, content, author_id, channel_id) VALUES (?, ?, ?, ?)";
            int rootEntryId;
            try (PreparedStatement ps = connection.prepareStatement(rootEntrySql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Sample Project");
                ps.setString(2, "This is a sample project for testing the Entry Management System.");
                ps.setInt(3, authorId);
                ps.setInt(4, startChannelId);
                ps.executeUpdate();
                
                rs = ps.getGeneratedKeys();
                rs.next();
                rootEntryId = rs.getInt(1);
                rs.close();
            }

            // Insert Section 1
            int section1EntryId;
            try (PreparedStatement ps = connection.prepareStatement(rootEntrySql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Section 1");
                ps.setString(2, "This is a section under the root entry");
                ps.setInt(3, authorId);
                ps.setInt(4, startChannelId + 1);
                ps.executeUpdate();
                
                rs = ps.getGeneratedKeys();
                rs.next();
                section1EntryId = rs.getInt(1);
                rs.close();
            }

            // Update Section 1's parent to point to root
            String updateParentSql = "UPDATE entries SET parent_id = ? WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateParentSql)) {
                ps.setInt(1, rootEntryId);
                ps.setInt(2, section1EntryId);
                ps.executeUpdate();
            }

            // Insert 4 subsections under Section 1
            String[] subsectionTitles = {"Subsection 1.1", "Subsection 1.2", "Subsection 1.3", "Subsection 1.4"};
            for (int i = 0; i < 4; i++) {
                try (PreparedStatement ps = connection.prepareStatement(rootEntrySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, subsectionTitles[i]);
                    ps.setString(2, "This is " + subsectionTitles[i].toLowerCase() + " under Section 1");
                    ps.setInt(3, authorId);
                    ps.setInt(4, startChannelId + 2 + i);
                    ps.executeUpdate();
                    
                    rs = ps.getGeneratedKeys();
                    rs.next();
                    int subsectionId = rs.getInt(1);
                    rs.close();
                    
                    // Update subsection's parent to point to Section 1
                    try (PreparedStatement ps2 = connection.prepareStatement(updateParentSql)) {
                        ps2.setInt(1, section1EntryId);
                        ps2.setInt(2, subsectionId);
                        ps2.executeUpdate();
                    }
                }
            }

            // Add EDITOR permission to "lez" user on root entry
            String permissionSql = "INSERT INTO entry_permissions(entry_id, username, permission) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(permissionSql)) {
                ps.setInt(1, rootEntryId);
                ps.setString(2, "lez");
                ps.setString(3, "EDITOR");
                ps.executeUpdate();
            }

            LOGGER.info("Successfully created mock entry hierarchy with 6 entries");
            LOGGER.info("Root entry ID: " + rootEntryId + ", User 'lez' has EDITOR permission");

        } catch (SQLException e) {
            LOGGER.severe("Error creating mock entry hierarchy: " + e.getMessage());
            throw new RuntimeException("Failed to create mock entry hierarchy", e);
        }
    }
}
