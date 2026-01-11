package fr.opal.factory;

import fr.opal.dao.ProjectDAO;

/**
 * Abstract factory for creating DAO objects
 */
public abstract class AbstractProjectFactory {
    private static AbstractProjectFactory instance;

    /**
     * Get singleton instance
     */
    public static AbstractProjectFactory getInstance() {
        if (instance == null) {
            instance = new MySQLFactory();
        }
        return instance;
    }

    /**
     * Create ProjectDAO instance
     */
    public abstract ProjectDAO createProjectDAO();
}
