package fr.opal.type;

import java.util.Date;

/**
 * Represents a comment on an entry
 */
public class Comment {
    private int id;
    private String content;
    private User author;
    private Date createdDate;

    /**
     * Constructor for creating a new comment
     */
    public Comment(String content, User author) {
        this.content = content;
        this.author = author;
        this.createdDate = new Date();
    }

    /**
     * Constructor with ID for loading from database
     */
    public Comment(int id, String content, User author, Date createdDate) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdDate = createdDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns a formatted string representation of the comment
     */
    @Override
    public String toString() {
        return author.getUsername() + " - " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(createdDate) +
               "\n" + content;
    }
}
