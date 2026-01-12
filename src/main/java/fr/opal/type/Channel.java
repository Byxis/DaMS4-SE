package fr.opal.type;

import java.util.Date;

/**
 * Represents a generic channel in the unified channel architecture.
 * Channels are simple containers for messages - the context (Entry comment section or DM)
 * is determined by the owning entity (Entry or Friendship).
 */
public class Channel {
    private int id;
    private Date createdAt;

    /**
     * Constructor for creating a new channel
     */
    public Channel() {
        this.createdAt = new Date();
    }

    /**
     * Constructor with ID for loading from database
     */
    public Channel(int id, Date createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Channel{id=" + id + ", createdAt=" + createdAt + "}";
    }
}
