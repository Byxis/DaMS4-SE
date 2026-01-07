package fr.opal.factory;

import fr.opal.dao.FriendsDAO;
import fr.opal.dao.MySQLFriendsDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * MySQL implementation of FriendsFactory.
 */
public class MySQLFriendsFactory extends FriendsFactory {

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
            Logger.getLogger("MySQLFriendsFactory").severe("File .env not found: " + e.getMessage());
        }
        return props;
    }

    /**
     * Gets or creates a database connection.
     *
     * @return The database connection
     * @throws SQLException if a database error occurs
     */
    private Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conn;
    }

    @Override
    public FriendsDAO createFriendsDAO() {
        try {
            return new MySQLFriendsDAO(getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create FriendsDAO", e);
        }
    }
}
