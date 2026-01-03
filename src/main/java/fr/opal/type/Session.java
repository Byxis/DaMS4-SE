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

    /**
     * Constructor
     */
    public Session(String id, Date creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    /**
     * Constructor with username and userId
     */
    public Session(String id, Date creationDate, String username, int userId) {
        this.id = id;
        this.creationDate = creationDate;
        this.username = username;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
