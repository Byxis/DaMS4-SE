package fr.opal.factory;

import fr.opal.dao.UserDAO;

/**
 * 
 */
public abstract class AbstractFactory {

    /**
     * Default constructor
     */
    public AbstractFactory() {
    }

    /**
     * 
     */
    private static AbstractFactory instance;


    /**
     * @return
     */
    synchronized public static AbstractFactory getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new MySQLFactory();
        return instance;
    }

    /**
     * @return
     */
    abstract public UserDAO createUserDAO();
}