package fr.opal.type;

import java.util.Date;

/**
 * Represents a message in the unified channel architecture.
 * Used for both Entry comments and DMs - context is derived from the channel's owner (Entry or Friendship).
 */
public class Message {
    private long id;
    private int channelId;
    private User sender;
    private String content;
    private Date createdAt;

    /**
     * Constructor for creating a new message
     */
    public Message(int channelId, User sender, String content) {
        this.channelId = channelId;
        this.sender = sender;
        this.content = content;
        this.createdAt = new Date();
    }

    /**
     * Constructor with ID for loading from database
     */
    public Message(long id, int channelId, User sender, String content, Date createdAt) {
        this.id = id;
        this.channelId = channelId;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters
    public long getId() {
        return id;
    }

    public int getChannelId() {
        return channelId;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns a formatted string representation of the message
     */
    @Override
    public String toString() {
        return sender.getUsername() + " - " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(createdAt) +
               "\n" + content;
    }
}
