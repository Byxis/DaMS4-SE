package fr.opal.factory;

import fr.opal.dao.PostgresUserDAO;
import fr.opal.dao.UserDAO;

import java.sql.*;
import java.util.logging.Logger;

/**
 *
 */
public class PostgresFactory extends AbstractFactory
{

    private static final String URL = "jdbc:postgresql://localhost:5432/opal_db";
    private static final String USER = "opal_user";
    private static final String PASSWORD = "opal_password";
    private Connection conn;

    /**
     * Default constructor
     */
    public PostgresFactory()
    {
        super();
        createConnection();
    }

    private void createConnection()
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        catch (SQLException | ClassNotFoundException _e)
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
            Logger.getLogger("PostgresFactory").severe("Error checking connection: " + _e.getMessage());
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
            Logger.getLogger("PostgresFactory").severe("Error creating users table: " + _e.getMessage());
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
        return new PostgresUserDAO(this.conn);
    }
}