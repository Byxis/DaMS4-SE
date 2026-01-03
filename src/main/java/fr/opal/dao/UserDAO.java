package fr.opal.dao;

import fr.opal.type.User;
import fr.opal.type.Permission;
import fr.opal.type.Profile;

import java.util.List;

/**
 * Abstract DAO for user operations
 */
public abstract class UserDAO {

    /**
     * Default constructor
     */
    public UserDAO() {
    }

    /**
     * Get user by username
     */
    abstract public User getUserById(String username);

    /**
     * Create a new user
     */
    abstract public User createUser(String username, String password);

    /**
     * Get user profile
     */
    abstract public Profile getProfile(int userId);

    /**
     * Update user profile
     */
    abstract public void updateProfile(int userId, Profile profile);

    /**
     * Get list of permissions for a user
     */
    abstract public List<Permission> listPermissions(int userId);

    /**
     * Create a new permission for a user
     */
    abstract public Permission createPermission(int userId, String permissionName);

    /**
     * Update an existing permission
     */
    abstract public void updatePermission(int permissionId, String permissionName);

    /**
     * Delete a permission
     */
    abstract public void deletePermission(int permissionId);
}