package fr.opal.service;

import fr.opal.dao.UserDAO;
import fr.opal.factory.AbstractFactory;
import fr.opal.type.User;

import java.util.*;
import java.util.logging.Logger;

/**
 * 
 */
public class UserManager {

    /**
     * Default constructor
     */
    public UserManager() {
    }

    /**
     * 
     */
    private static UserManager instance;


    /**
     * 
     */
    private User connectedUser;



    /**
     * 
     */
    private Map<String, User> connectedUsers = new HashMap<String, User>();

    /**
     * @return
     */
    public static UserManager getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new UserManager();
        return instance;
    }

    /**
     * @param id 
     * @param pass 
     * @return
     */
    public boolean login(String id, String pass) {
        AbstractFactory factory = AbstractFactory.getInstance();
        if (connectedUsers.containsKey(id))
        {
            User user = connectedUsers.get(id);
            if (user.getPassword().equalsIgnoreCase(pass)) {
                Logger.getLogger("UserManager").info("User already connected: " + id);
                connectedUser = user;
                connectedUsers.put(id, user);
                return true;
            }
        }
        else
        {
            User user = factory.createUserDAO().getUserById(id);
            if (user != null && user.getPassword().equalsIgnoreCase(pass)) {
                Logger.getLogger("UserManager").info("User logged in: " + id);
                connectedUser = user;
                connectedUsers.put(id, user);
                return true;
            }
        }
        Logger.getLogger("UserManager").info("Failed login attempt for user: " + id);
        return false;
    }

}