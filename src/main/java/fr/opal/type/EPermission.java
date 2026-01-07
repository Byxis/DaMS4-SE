package fr.opal.type;

/**
 * Enum representing user permissions on entries
 */
public enum EPermission {
    READER("READER"),
    COMMENTOR("COMMENTOR"),
    EDITOR("EDITOR");

    private final String displayName;

    /**
     * Constructor
     */
    EPermission(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of the permission
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this permission includes VIEW capability
     */
    public boolean canView() {
        return this != null;
    }

    /**
     * Checks if this permission includes COMMENT capability
     */
    public boolean canComment() {
        return this == COMMENTOR || this == EDITOR;
    }

    /**
     * Checks if this permission includes EDIT capability
     */
    public boolean canEdit() {
        return this == EDITOR;
    }
}
