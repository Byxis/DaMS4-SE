package fr.opal.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages permissions for users on an entry
 */
public class EntryPermissionManager {
    private List<UserPermission> userPermissions;

    /**
     * Constructor
     */
    public EntryPermissionManager() {
        this.userPermissions = new ArrayList<>();
    }

    /**
     * Adds a user permission for the entry
     */
    public void addUserPermission(UserPermission permission) {
        // Remove existing permission for this user if present
        userPermissions.removeIf(up -> up.getUser().getId() == permission.getUser().getId());
        userPermissions.add(permission);
    }

    /**
     * Removes a user permission
     */
    public void removeUserPermission(User user) {
        userPermissions.removeIf(up -> up.getUser().getId() == user.getId());
    }

    /**
     * Gets the permission for a specific user
     */
    public UserPermission getUserPermission(User user) {
        for (UserPermission up : userPermissions) {
            if (up.getUser().getId() == user.getId()) {
                return up;
            }
        }
        return null;
    }

    /**
     * Checks if a user can view the entry
     */
    public boolean canView(User user) {
        UserPermission up = getUserPermission(user);
        return up != null && up.getPermission().canView();
    }

    /**
     * Checks if a user can edit the entry
     */
    public boolean canEdit(User user) {
        UserPermission up = getUserPermission(user);
        return up != null && up.getPermission().canEdit();
    }

    /**
     * Checks if a user can comment on the entry
     */
    public boolean canComment(User user) {
        UserPermission up = getUserPermission(user);
        return up != null && up.getPermission().canComment();
    }

    /**
     * Gets all user permissions for this entry
     */
    public List<UserPermission> getUserPermissions() {
        return new ArrayList<>(userPermissions);
    }

    /**
     * Sets all user permissions for this entry
     */
    public void setUserPermissions(List<UserPermission> permissions) {
        this.userPermissions = new ArrayList<>(permissions);
    }

    /**
     * Gets all user permissions for this entry
     */
    public List<UserPermission> getAllPermissions() {
        return new ArrayList<>(userPermissions);
    }
}

