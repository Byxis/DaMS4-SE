package fr.opal.type;

/**
 * Represents a user profile
 */
public class Profile {

    private int userId;
    private String displayName;
    private String bio;
    private String contactInfo;

    /**
     * Constructor
     */
    public Profile(int userId, String displayName, String bio, String contactInfo) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.contactInfo = contactInfo;
    }

    public int getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void update(String displayName, String bio, String contactInfo) {
        this.displayName = displayName;
        this.bio = bio;
        this.contactInfo = contactInfo;
    }
}
