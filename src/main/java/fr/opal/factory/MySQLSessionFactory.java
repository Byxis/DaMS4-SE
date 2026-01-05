package fr.opal.factory;

import fr.opal.dao.MySQLSessionDAO;
import fr.opal.dao.SessionDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * MySQL implementation of SessionDAOFactory
 */
public class MySQLSessionFactory extends SessionDAOFactory {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private Connection conn;

    static {
        Properties props = loadEnvFile();
        URL = props.getProperty("DB_URL");
        USER = props.getProperty("DB_USER");
        PASSWORD = props.getProperty("DB_PASSWORD");

        if (URL == null || USER == null || PASSWORD == null) {
            throw new RuntimeException("Missing environment variables in .env");
        }
    }

    private static Properties loadEnvFile() {
        Properties props = new Properties();
        try {
            Files.lines(Paths.get(".env"))
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            props.setProperty(parts[0].trim(), parts[1].trim());
                        }
                    });
        } catch (IOException e) {
            Logger.getLogger("MySQLSessionFactory").severe("File .env not found: " + e.getMessage());
        }
        return props;
    }

    public MySQLSessionFactory() {
        super();
        createConnection();
    }

    private void createConnection() {
        try {
            this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createTable();
    }

    private boolean isConnected() {
        try {
            return this.conn != null && !this.conn.isClosed();
        } catch (SQLException e) {
            Logger.getLogger("MySQLSessionFactory").severe("Error checking connection: " + e.getMessage());
            return false;
        }
    }

    private void createTable() {
        if (!isConnected()) {
            createConnection();
        }

        String sql = "CREATE TABLE IF NOT EXISTS session_settings (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "user_id INT NOT NULL UNIQUE," +
                "font_size INT DEFAULT 14," +
                "style_palette VARCHAR(20) DEFAULT 'LIGHT'," +
                "accent_color VARCHAR(20) DEFAULT 'BLACK'," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Logger.getLogger("MySQLSessionFactory").severe("Error creating table: " + e.getMessage());
        }
    }

    @Override
    public SessionDAO createSessionDAO() {
        if (!isConnected()) {
            createConnection();
        }
        return new MySQLSessionDAO(conn);
    }
}
