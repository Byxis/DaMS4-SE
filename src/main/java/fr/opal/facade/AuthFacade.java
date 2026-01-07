package fr.opal.facade;

import fr.opal.service.AuthManager;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.type.User;

/**
 * Facade for authentication and authorization operations
 */
public class AuthFacade {
    private static AuthFacade instance;
    private Session currentSession;

    public static AuthFacade getInstance() {
        if (instance == null) {
            instance = new AuthFacade();
        }
        return instance;
    }

    private AuthFacade() {
    }

    /**
     * Authenticates a user with username and password
     */
    public Session login(String username, String password) {
        Session session = AuthManager.getInstance().authenticate(username, password);
        if (session != null) {
            this.currentSession = session;
        }
        return session;
    }

    /**
     * Logs out a user by terminating their session
     */
    public void logout(String sessionId) {
        AuthManager.getInstance().terminateSession(sessionId);
        this.currentSession = null;
    }

    /**
     * Retrieves user profile
     */
    public Profile getProfile(int userId) {
        return AuthManager.getInstance().getProfile(userId);
    }

    /**
     * Updates user profile information
     */
    public void updateProfile(int userId, Profile profile) {
        AuthManager.getInstance().updateProfile(userId, profile);
    }

    /**
     * Registers a new user
     */
    public User register(String username, String password) {
        return AuthManager.getInstance().register(username, password);
    }

    /**
     * Gets the current session
     */
    public Session getCurrentSession() {
        return currentSession;
    }

    /**
     * Sets the current session
     */
    public void setCurrentSession(Session session) {
        this.currentSession = session;
    }
}
