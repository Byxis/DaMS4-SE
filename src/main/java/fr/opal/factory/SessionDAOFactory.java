package fr.opal.factory;

import fr.opal.dao.SessionDAO;

/**
 * Abstract factory for creating SessionDAO instances
 */
public abstract class SessionDAOFactory {

    private static SessionDAOFactory instance;

    /**
     * Default constructor
     */
    public SessionDAOFactory() {
    }

    /**
     * Get singleton instance of SessionDAOFactory
     * @return The factory instance
     */
    public static synchronized SessionDAOFactory getInstance() {
        if (instance == null) {
            instance = new MySQLSessionFactory();
        }
        return instance;
    }

    /**
     * Create a SessionDAO instance
     * @return A new SessionDAO
     */
    public abstract SessionDAO createSessionDAO();
}
