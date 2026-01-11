package fr.opal.facade;

import fr.opal.service.AuthManager;
import fr.opal.type.Permission;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.type.User;

import java.util.List;

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
     * Retrieves list of permissions for a user
     */
    public List<Permission> listPermissions(int userId) {
        return AuthManager.getInstance().listPermissions(userId);
    }

    /**
     * Creates a new permission for a user
     */
    public Permission createPermission(int userId, String permissionName) {
        return AuthManager.getInstance().createPermission(userId, permissionName);
    }

    /**
     * Updates an existing permission
     */
    public void updatePermission(int permissionId, String permissionName) {
        AuthManager.getInstance().updatePermission(permissionId, permissionName);
    }

    /**
     * Deletes a permission
     */
    public void deletePermission(int permissionId) {
        AuthManager.getInstance().deletePermission(permissionId);
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

    /**
     * Check if a user exists by username
     */
    public boolean userExists(String username) {
        return AuthManager.getInstance().userExists(username);
    }
}
