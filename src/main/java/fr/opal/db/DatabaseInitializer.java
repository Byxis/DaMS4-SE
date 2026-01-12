package fr.opal.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Handles database schema initialization and updates.
 */
public class DatabaseInitializer
{
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    /**
     * Initializes the database schema and ensures it is up to date.
     */
    public static void initialize(Connection connection)
    {
        try (Statement stmt = connection.createStatement())
        {
            LOGGER.info("Starting database schema initialization...");

            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(50) UNIQUE NOT NULL,"
                + "password VARCHAR(100) NOT NULL"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS projects ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "name VARCHAR(255) NOT NULL,"
                + "description TEXT,"
                + "owner_id INT NOT NULL,"
                + "state ENUM('PRIVATE', 'PUBLIC', 'ARCHIVED') DEFAULT 'PRIVATE',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS project_collaborators ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "project_id INT NOT NULL,"
                + "username VARCHAR(50) NOT NULL,"
                + "permission ENUM('OWNER', 'CONTRIBUTOR', 'READER') DEFAULT 'READER',"
                + "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,"
                + "UNIQUE KEY unique_collaborator (project_id, username)"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS project_tags ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "project_id INT NOT NULL,"
                + "tag VARCHAR(50) NOT NULL,"
                + "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,"
                + "UNIQUE KEY unique_tag (project_id, tag)"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS project_invitations ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "project_id INT NOT NULL,"
                + "invited_username VARCHAR(50) NOT NULL,"
                + "inviter_username VARCHAR(50) NOT NULL,"
                + "suggested_permission ENUM('OWNER', 'CONTRIBUTOR', 'READER') DEFAULT 'READER',"
                + "status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED') DEFAULT 'PENDING',"
                + "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "responded_at TIMESTAMP NULL,"
                + "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS notifications ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id INT NOT NULL,"
                + "content TEXT NOT NULL,"
                + "type ENUM('PROJECT', 'SOCIAL', 'GENERAL', 'INVITATION', 'COMMENT') DEFAULT 'GENERAL',"
                + "status ENUM('TO_READ', 'READ', 'HIDDEN') DEFAULT 'TO_READ',"
                + "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "INDEX idx_user_status (user_id, status),"
                + "INDEX idx_creation_date (creation_date)"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS channels ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB;");

            stmt.execute("CREATE TABLE IF NOT EXISTS messages ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                + "channel_id INT NOT NULL,"
                + "sender_id INT NOT NULL,"
                + "content TEXT NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX idx_channel_created (channel_id, created_at DESC)"
                + ") ENGINE=InnoDB;");

            ensureColumnExists(stmt, "friendships", "channel_id", "INT UNIQUE NULL");
            ensureColumnExists(stmt, "entries", "channel_id", "INT UNIQUE NOT NULL");

            createMissingFriendshipChannels(connection);

            LOGGER.info("Database schema initialized successfully.");
        }
        catch (SQLException e)
        {
            LOGGER.severe("Failed to initialize database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failure", e);
        }
    }

    /**
     * Helper to add a column if it doesn't exist.
     */
    private static void ensureColumnExists(Statement stmt, String table, String column, String definition)
    {
        try
        {
            stmt.executeQuery("SELECT " + column + " FROM " + table + " LIMIT 1").close();
            LOGGER.fine(table + "." + column + " column already exists");
        }
        catch (SQLException e)
        {
            LOGGER.info("Adding " + column + " column to " + table + " table...");
            try
            {
                stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
            catch (SQLException ex)
            {
                LOGGER.warning("Could not add column " + column + " to " + table + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Creates channels for any accepted friendships that are missing a channel_id.
     */
    private static void createMissingFriendshipChannels(Connection connection)
    {
        String findMissing = "SELECT id FROM friendships WHERE status = 'ACCEPTED' AND channel_id IS NULL";
        String createChannel = "INSERT INTO channels() VALUES()";
        String updateFriendship = "UPDATE friendships SET channel_id = ? WHERE id = ?";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(findMissing))
        {
            int count = 0;
            while (rs.next())
            {
                int friendshipId = rs.getInt("id");
                try (PreparedStatement createPs =
                         connection.prepareStatement(createChannel, Statement.RETURN_GENERATED_KEYS))
                {
                    createPs.executeUpdate();
                    try (ResultSet keyRs = createPs.getGeneratedKeys())
                    {
                        if (keyRs.next())
                        {
                            int channelId = keyRs.getInt(1);
                            try (PreparedStatement updatePs = connection.prepareStatement(updateFriendship))
                            {
                                updatePs.setInt(1, channelId);
                                updatePs.setInt(2, friendshipId);
                                updatePs.executeUpdate();
                                count++;
                            }
                        }
                    }
                }
            }
            if (count > 0)
                LOGGER.info("Created channels for " + count + " existing friendships");
        }
        catch (SQLException e)
        {
            LOGGER.warning("Error creating missing friendship channels: " + e.getMessage());
        }
    }
}