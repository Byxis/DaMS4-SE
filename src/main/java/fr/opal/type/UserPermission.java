package fr.opal.type;

/**
 * Represents a user's permission on an entry
 */
public class UserPermission {
    private User user;
    private EPermission permission;

    /**
     * Constructor
     */
    public UserPermission(User user, EPermission permission) {
        this.user = user;
        this.permission = permission;
    }

    // Getters
    public User getUser() {
        return user;
    }

    public EPermission getPermission() {
        return permission;
    }

    // Setters
    public void setUser(User user) {
        this.user = user;
    }

    public void setPermission(EPermission permission) {
        this.permission = permission;
    }
}
