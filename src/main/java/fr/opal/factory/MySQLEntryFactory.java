package fr.opal.factory;

import fr.opal.type.Entry;
import fr.opal.type.User;
import fr.opal.dao.MySQLEntryDAO;
import fr.opal.dao.EntryDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * MySQL implementation of EntryFactory
 */
public class MySQLEntryFactory extends AbstractEntryFactory {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private Connection conn;
    private MySQLEntryDAO dao;

    static {
        Properties props = loadEnvFile();
        URL = props.getProperty("DB_URL");
        USER = props.getProperty("DB_USER");
        PASSWORD = props.getProperty("DB_PASSWORD");

        if (URL == null || USER == null || PASSWORD == null) {
            throw new RuntimeException("Variables d'environnement manquantes dans .env");
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
            Logger.getLogger("MySQLEntryFactory").severe("Fichier .env introuvable: " + e.getMessage());
        }
        return props;
    }

    /**
     * Constructor
     */
    public MySQLEntryFactory() {
        super();
        createConnection();
    }

    /**
     * Creates a new database connection
     */
    private void createConnection() {
        try {
            this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if connection is still active
     */
    private boolean isConnected() {
        try {
            return this.conn != null && !this.conn.isClosed();
        } catch (SQLException e) {
            Logger.getLogger("MySQLEntryFactory").severe("Error checking connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a new Entry
     */
    @Override
    public Entry createEntry(String title, String content, User author) {
        return new Entry(title, content, author);
    }

    /**
     * Creates an Entry with an ID (for loading from database)
     */
    @Override
    public Entry createEntry(int id, String title, String content, User author) {
        return new Entry(id, title, content, author);
    }

    /**
     * Gets the DAO for the factory's storage type
     */
    @Override
    public EntryDAO getEntryDAO() {
        if (!isConnected()) {
            createConnection();
        }
        this.dao = new MySQLEntryDAO(this.conn);
        return dao;
    }
}
