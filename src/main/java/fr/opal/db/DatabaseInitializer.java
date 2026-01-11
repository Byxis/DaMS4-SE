package fr.opal.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Handles database schema initialization.
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

            // Session Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS session_settings (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id INT NOT NULL UNIQUE," +
                    "font_size INT DEFAULT 14," +
                    "style_palette VARCHAR(20) DEFAULT 'LIGHT'," +
                    "accent_color VARCHAR(20) DEFAULT 'BLACK'," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // Friendships 
            stmt.execute("CREATE TABLE IF NOT EXISTS friendships (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id1 INT NOT NULL," +
                    "user_id2 INT NOT NULL," +
                    "status ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id1) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (user_id2) REFERENCES users(id) ON DELETE CASCADE," +
                    "CHECK (user_id1 != user_id2)" +
                    ");");

            // Follows
            stmt.execute("CREATE TABLE IF NOT EXISTS follows (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "follower_id INT NOT NULL," +
                    "followed_id INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_follow (follower_id, followed_id)," +
                    "CHECK (follower_id != followed_id)" +
                    ");");

            // Blocks
            stmt.execute("CREATE TABLE IF NOT EXISTS blocks (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "blocker_id INT NOT NULL," +
                    "blocked_id INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_block (blocker_id, blocked_id)," +
                    "CHECK (blocker_id != blocked_id)" +
                    ");");
            
            // Permissions
            stmt.execute("CREATE TABLE IF NOT EXISTS permissions (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id INT NOT NULL," +
                    "name VARCHAR(255) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // Profiles
            stmt.execute("CREATE TABLE IF NOT EXISTS user_profiles (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id INT NOT NULL UNIQUE," +
                    "display_name VARCHAR(255)," +
                    "bio TEXT," +
                    "contact_info VARCHAR(255)," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // Sessions (Optional, but if kept should match schema)
            stmt.execute("CREATE TABLE IF NOT EXISTS sessions (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // Entries Table
            stmt.execute("CREATE TABLE IF NOT EXISTS entries (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "content LONGTEXT," +
                    "parent_id INT," +
                    "author_id INT NOT NULL," +
                    "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (parent_id) REFERENCES entries(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (author_id) REFERENCES users(id)" +
                    ");");

            // Comments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "entry_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "author_id INT NOT NULL," +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (entry_id) REFERENCES entries(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (author_id) REFERENCES users(id)" +
                    ");");

            // Entry Permissions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS entry_permissions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "entry_id INT NOT NULL," +
                    "username VARCHAR(255) NOT NULL," +
                    "permission VARCHAR(50)," +
                    "FOREIGN KEY (entry_id) REFERENCES entries(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_entry_user (entry_id, username)" +
                    ");");

        } catch (SQLException e) {
            LOGGER.severe("Error initializing database tables: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
