package fr.opal.factory;

import fr.opal.dao.*;
import fr.opal.db.DatabaseManager;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * MySQL implementation of the DAO Factory.
 */
public class MySQLDAOFactory extends AbstractDAOFactory
{
    private final Connection connection;

    public MySQLDAOFactory()
    {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override public UserDAO createUserDAO()
    {
        return new MySQLUserDAO(connection);
    }

    @Override public SessionDAO createSessionDAO()
    {
        return new MySQLSessionDAO(connection);
    }

    @Override public NotificationDAO createNotificationDAO()
    {
        return new MySQLNotificationDAO(connection);
    }

    @Override public ProjectDAO createProjectDAO()
    {
        return new MySQLProjectDAO(connection);
    }
}
