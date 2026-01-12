package fr.opal.dao;

import fr.opal.type.Permission;
import fr.opal.type.User;
import fr.opal.type.Profile;

import java.util.List;

/**
 * Abstract DAO for user operations
 */
public abstract class UserDAO {

    /**
     * Get user by username
     */
    public abstract User getUserById(String username);

    /**
     * Get user by database ID (integer primary key)
     */
    abstract public User getUserByDatabaseId(int id);

    /**
     * Create a new user
     */
    public abstract User createUser(String username, String password);

    /**
     * Get user profile
     */
    public abstract Profile getProfile(int userId);

    /**
     * Update user profile
     */
    public abstract void updateProfile(int userId, Profile profile);

    /**
     * Get list of permissions for a user
     */
    public abstract List<Permission> listPermissions(int userId);

    /**
     * Create a new permission for a user
     */
    public abstract Permission createPermission(int userId, String permissionName);

    /**
     * Update an existing permission
     */
    public abstract void updatePermission(int permissionId, String permissionName);

    /**
     * Delete a permission
     */
    public abstract void deletePermission(int permissionId);
}