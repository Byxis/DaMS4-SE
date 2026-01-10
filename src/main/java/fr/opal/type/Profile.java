package fr.opal.type;

/**
 * Represents a user profile
 */
public class Profile {

    private int userId;
    private String displayName;
    private String bio;
    private String contactInfo;

    public Profile(int userId, String displayName, String bio, String contactInfo) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.contactInfo = contactInfo;
    }

    /**
     * Gets the user ID associated with this profile.
     *
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the display name of the user.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the biography of the user.
     *
     * @return the biography
     */
    public String getBio() {
        return bio;
    }

    /**
     * Gets the contact information of the user.
     *
     * @return the contact information
     */
    public String getContactInfo() {
        return contactInfo;
    }

    /**
     * Updates the profile information.
     *
     * @param displayName the new display name
     * @param bio the new biography
     * @param contactInfo the new contact information
     */
    public void update(String displayName, String bio, String contactInfo) {
        this.displayName = displayName;
        this.bio = bio;
        this.contactInfo = contactInfo;
    }
}
