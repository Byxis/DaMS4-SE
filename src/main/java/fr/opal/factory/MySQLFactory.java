package fr.opal.factory;

import fr.opal.dao.*;
import fr.opal.db.DatabaseInitializer;
import fr.opal.db.DatabaseManager;

import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Concrete factory implementation for MySQL (deprecated)
 * Use AbstractDAOFactory.getFactory() instead for centralized DAO creation
 * Kept for backward compatibility only
 */
@Deprecated
public class MySQLFactory extends AbstractProjectFactory {

    private final Connection connection;
    private static final Logger LOGGER = Logger.getLogger(MySQLFactory.class.getName());

    /**
     * Constructor initializes database connection
     */
    public MySQLFactory()
    {
        this.connection = DatabaseManager.getInstance().getConnection();
        DatabaseInitializer.initialize(this.connection);
    }

    /**
     * Create MySQL ProjectDAO instance (deprecated)
     *
     * @return the ProjectDAO
     */
    @Override
    public ProjectDAO createProjectDAO()
    {
        return new MySQLProjectDAO(connection);
    }
}
