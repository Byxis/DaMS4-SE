package fr.opal.factory;

import fr.opal.dao.MySQLUserDAO;
import fr.opal.dao.UserDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class MySQLFactory extends AbstractFactory
{
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private Connection conn;

    static
    {
        Properties props = loadEnvFile();
        URL = props.getProperty("DB_URL");
        USER = props.getProperty("DB_USER");
        PASSWORD = props.getProperty("DB_PASSWORD");

        if (URL == null || USER == null || PASSWORD == null)
        {
            throw new RuntimeException("Variables d'environnement manquantes dans .env");
        }
    }

    private static Properties loadEnvFile()
    {
        Properties props = new Properties();
        try
        {
            Files.lines(Paths.get(".env"))
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2)
                        {
                            props.setProperty(parts[0].trim(), parts[1].trim());
                        }
                    });
        }
        catch (IOException e)
        {
            Logger.getLogger("MySQLFactory").severe("Fichier .env introuvable: " + e.getMessage());
        }
        return props;
    }

    public MySQLFactory()
    {
        super();
        createConnection();
    }

    private void createConnection()
    {
        try
        {
            this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        catch (SQLException _e)
        {
            throw new RuntimeException(_e);
        }
        createTable(conn);
    }

    private boolean isConnected()
    {
        try
        {
            return this.conn != null && !this.conn.isClosed();
        }
        catch (SQLException _e)
        {
            Logger.getLogger("MySQLFactory").severe("Error checking connection: " + _e.getMessage());
            return false;
        }
    }

    private void createTable(Connection _conn)
    {
        if (!isConnected())
        {
            createConnection();
        }

        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL" +
                ");";
        try (Statement stmt = _conn.createStatement())
        {
            stmt.execute(sql);
            System.out.println("Table `users` créée ou déjà existante.");
        }
        catch (SQLException _e)
        {
            Logger.getLogger("MySQLFactory").severe("Error creating users table: " + _e.getMessage());
        }
    }


    /**
     * @return
     */
    public UserDAO createUserDAO()
    {
        if (!isConnected())
        {
            createConnection();
        }
        return new MySQLUserDAO(this.conn);
    }
}