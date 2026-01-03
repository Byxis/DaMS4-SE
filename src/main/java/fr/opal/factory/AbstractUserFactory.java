package fr.opal.factory;

import fr.opal.dao.UserDAO;

/**
 * 
 */
public abstract class AbstractUserFactory
{

    /**
     * Default constructor
     */
    public AbstractUserFactory() {
    }

    /**
     * 
     */
    private static AbstractUserFactory instance;


    /**
     * @return
     */
    synchronized public static AbstractUserFactory getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new MySQLUserFactory();
        return instance;
    }

    /**
     * @return
     */
    abstract public UserDAO createUserDAO();
}