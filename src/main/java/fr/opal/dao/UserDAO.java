package fr.opal.dao;

import fr.opal.type.User;

/**
 * 
 */
public abstract class UserDAO {


    /**
     * Default constructor
     */
    public UserDAO() {
    }


    /**
     * @return
     */
    abstract public User getUserById(String username);
    abstract public User createUser(String username, String password);
}