package fr.opal.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Singleton class to manage database connection.
 * Follows the Singleton pattern to ensure only one instance handles the connection.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    private Connection connection;

    static {
        Properties props = loadEnvFile();
        URL = props.getProperty("DB_URL");
        USER = props.getProperty("DB_USER");
        PASSWORD = props.getProperty("DB_PASSWORD");

        if (URL == null || USER == null || PASSWORD == null) {
            throw new RuntimeException("Missing environment variables in .env (DB_URL, DB_USER, DB_PASSWORD)");
        }
    }

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
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
            LOGGER.severe("File .env not found: " + e.getMessage());
        }
        return props;
    }

    /**
     * Returns the active connection. Reconnects if the connection is closed or null.
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                LOGGER.info("Database connection established.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException("Database connection failure", e);
        }
        return connection;
    }
}
