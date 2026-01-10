package fr.opal.type;

import java.util.Date;

/**
 * Represents a user session
 */
public class Session {

    private String id;
    private Date creationDate;
    private String username;
    private int userId;

    public Session(String id, Date creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public Session(String id, Date creationDate, String username, int userId) {
        this.id = id;
        this.creationDate = creationDate;
        this.username = username;
        this.userId = userId;
    }

    /**
     * Get the session ID
     *
     * @return the session ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the session creation date
     *
     * @return the session creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Get the username associated with the session
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the user ID associated with the session
     *
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set the session ID
     * @param id the session ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the session creation date
     * @param creationDate the session creation date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Set the username associated with the session
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the user ID associated with the session
     * @param userId the user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
