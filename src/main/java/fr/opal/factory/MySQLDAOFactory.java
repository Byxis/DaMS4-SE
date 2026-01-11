package fr.opal.factory;

import fr.opal.dao.*;
import fr.opal.db.DatabaseInitializer;
import fr.opal.db.DatabaseManager;

import java.sql.Connection;
import java.util.logging.Logger;

/**
 * MySQL implementation of the DAO Factory.
 * Centralizes creation of all DAOs for project management.
 */
public class MySQLDAOFactory extends AbstractDAOFactory {

    private final Connection connection;
    private static final Logger LOGGER = Logger.getLogger(MySQLDAOFactory.class.getName());

    /**
     * Constructor initializes database connection
     */
    public MySQLDAOFactory()
    {
        this.connection = DatabaseManager.getInstance().getConnection();
        DatabaseInitializer.initialize(this.connection);
    }

    /**
     * Create MySQL ProjectDAO instance
     *
     * @return the ProjectDAO
     */
    @Override
    public ProjectDAO createProjectDAO()
    {
        return new MySQLProjectDAO(connection);
    }
}
