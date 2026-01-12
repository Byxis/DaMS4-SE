package fr.opal.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Handles database schema initialization for project management.
 */
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    public static void initialize(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            // Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(100) NOT NULL" +
                    ");");

            // Projects Table
            stmt.execute("CREATE TABLE IF NOT EXISTS projects (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) NOT NULL," +
                    "description TEXT," +
                    "owner_id INT NOT NULL," +
                    "state ENUM('PRIVATE', 'PUBLIC', 'ARCHIVED') DEFAULT 'PRIVATE'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // Project Collaborators Table
            stmt.execute("CREATE TABLE IF NOT EXISTS project_collaborators (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "project_id INT NOT NULL," +
                    "username VARCHAR(50) NOT NULL," +
                    "permission ENUM('OWNER', 'CONTRIBUTOR', 'READER') DEFAULT 'READER'," +
                    "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_collaborator (project_id, username)" +
                    ");");

            // Project Tags Table
            stmt.execute("CREATE TABLE IF NOT EXISTS project_tags (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "project_id INT NOT NULL," +
                    "tag VARCHAR(50) NOT NULL," +
                    "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_tag (project_id, tag)" +
                    ");");

            // Project Invitations Table
            stmt.execute("CREATE TABLE IF NOT EXISTS project_invitations (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "project_id INT NOT NULL," +
                    "invited_username VARCHAR(50) NOT NULL," +
                    "inviter_username VARCHAR(50) NOT NULL," +
                    "suggested_permission ENUM('OWNER', 'CONTRIBUTOR', 'READER') DEFAULT 'READER'," +
                    "status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED') DEFAULT 'PENDING'," +
                    "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "responded_at TIMESTAMP NULL," +
                    "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE" +
                    ");");

            // Notifications Table
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "type ENUM('PROJECT', 'SOCIAL', 'GENERAL', 'INVITATION', 'COMMENT') DEFAULT 'GENERAL'," +
                    "status ENUM('TO_READ', 'READ', 'HIDDEN') DEFAULT 'TO_READ'," +
                    "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "INDEX idx_user_status (user_id, status)," +
                    "INDEX idx_creation_date (creation_date)" +
                    ");");

            LOGGER.info("Database schema initialized successfully.");
        } catch (SQLException e) {
            LOGGER.severe("Failed to initialize database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failure", e);
        }
    }
}
