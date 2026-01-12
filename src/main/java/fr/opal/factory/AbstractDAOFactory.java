package fr.opal.factory;

import fr.opal.dao.NotificationDAO;
import fr.opal.dao.ProjectDAO;
import fr.opal.dao.SessionDAO;
import fr.opal.dao.UserDAO;

/**
 * Abstract DAO Factory.
 * Centralizes the creation of all DAOs.
 * Implements the Abstract Factory pattern.
 */
public abstract class AbstractDAOFactory {

    /**
     * Enum for supported factory types.
     */
    public enum FactoryType {
        MYSQL,
        // XML, JSON, etc. can be added here
    }

    private static AbstractDAOFactory instance;

    /**
     * Returns the singleton instance of the DAO Factory.
     * Starts with a default MySQL factory.
     * @return The AbstractDAOFactory instance
     */
    public static synchronized AbstractDAOFactory getFactory() {
        if (instance == null) {
            // Default to MySQL
            instance = new MySQLDAOFactory();
        }
        return instance;
    }

    public abstract UserDAO createUserDAO();
    public abstract SessionDAO createSessionDAO();
    public abstract NotificationDAO createNotificationDAO();
    public abstract ProjectDAO createProjectDAO();
}
