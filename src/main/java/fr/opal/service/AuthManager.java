package fr.opal.service;

import fr.opal.factory.AbstractDAOFactory;
import fr.opal.type.Permission;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.type.User;

import java.util.*;

/**
 * Manager for authentication and authorization
 */
public class AuthManager {

    private static AuthManager instance;
    private User connectedUser;
    private Map<String, Session> activeSessions = new HashMap<>();
    private Map<String, User> connectedUsers = new HashMap<>();

    /**
     * Private constructor for singleton pattern
     */
    private AuthManager() {
    }

    /**
     * Get singleton instance
     */
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    /**
     * Authenticates a user with username and password
     * Returns a Session if authentication succeeds, null otherwise
     */
    public Session authenticate(String username, String password) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        
        if (connectedUsers.containsKey(username)) {
            User user = connectedUsers.get(username);
            if (user != null && user.verifyPassword(password)) {
                connectedUser = user;
                return createSession(username);
            }
        }
        
        User user = factory.createUserDAO().getUserById(username);
        if (user != null && user.verifyPassword(password)) {
            connectedUser = user;
            connectedUsers.put(username, user);
            return createSession(username);
        }
        
        return null;
    }

    /**
     * Creates a new session for the authenticated user
     */
    private Session createSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, new Date(), username, connectedUser.getId());
        activeSessions.put(sessionId, session);
        return session;
    }

    /**
     * Terminates a user session
     */
    public void terminateSession(String sessionId) {
        if (activeSessions.containsKey(sessionId)) {
            activeSessions.remove(sessionId);
            connectedUser = null;
            connectedUsers.clear();
        }
    }

    /**
     * Registers a new user
     */
    public User register(String username, String password) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        
        User existingUser = factory.createUserDAO().getUserById(username);
        if (existingUser != null) {
            return null;
        }
        
        User newUser = factory.createUserDAO().createUser(username, password);
        return newUser;
    }

    /**
     * Retrieves user profile
     */
    public Profile getProfile(int userId) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        return factory.createUserDAO().getProfile(userId);
    }

    /**
     * Updates user profile information
     */
    public void updateProfile(int userId, Profile profile) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        factory.createUserDAO().updateProfile(userId, profile);
    }

    /**
     * Retrieves list of permissions for a user
     */
    public List<Permission> listPermissions(int userId) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        return factory.createUserDAO().listPermissions(userId);
    }

    /**
     * Creates a new permission for a user
     */
    public Permission createPermission(int userId, String permissionName) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        return factory.createUserDAO().createPermission(userId, permissionName);
    }

    /**
     * Updates an existing permission
     */
    public void updatePermission(int permissionId, String permissionName) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        factory.createUserDAO().updatePermission(permissionId, permissionName);
    }

    /**
     * Deletes a permission
     */
    public void deletePermission(int permissionId) {
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        factory.createUserDAO().deletePermission(permissionId);
    }

    /**
     * Get currently connected user
     */
    public User getConnectedUser() {
        return connectedUser;
    }

    /**
     * Get session by ID
     */
    public Session getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Check if a user exists by username
     */
    public boolean userExists(String username) {
        if (connectedUsers.containsKey(username)) {
            return true;
        }
        AbstractDAOFactory factory = AbstractDAOFactory.getFactory();
        User user = factory.createUserDAO().getUserById(username);
        return user != null;
    }
}
