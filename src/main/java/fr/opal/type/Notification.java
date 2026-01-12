package fr.opal.type;

import java.util.Date;

/**
 * Represents a notification entity
 */
public class Notification {

    private int id;
    private String content;
    private Date creationDate;
    private User user;
    private ENotifType type;
    private ENotifStatus status;

    /**
     * Default empty constructor
     */
    public Notification() {
        this(0, "", new Date(), null, ENotifType.GENERAL, ENotifStatus.TO_READ);
    }

    /**
     * Constructor with all parameters
     *
     * @param id the notification ID
     * @param content the notification content
     * @param creationDate the creation date
     * @param user the target user
     * @param type the notification type
     * @param status the notification status
     */
    public Notification(int id, String content, Date creationDate, User user, ENotifType type, ENotifStatus status) {
        this.id = id;
        this.content = content;
        this.creationDate = creationDate;
        this.user = user;
        this.type = type;
        this.status = status;
    }

    /**
     * Constructor for creating new notification (without ID)
     *
     * @param content the notification content
     * @param user the target user
     * @param type the notification type
     */
    public Notification(String content, User user, ENotifType type) {
        this(0, content, new Date(), user, type, ENotifStatus.TO_READ);
    }

    /**
     * Get the notification ID
     *
     * @return the notification ID
     */
    public int getId() {
        return id;
    }

    /**
     * Set the notification ID
     *
     * @param id the notification ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the notification content
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the notification content
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the creation date
     *
     * @return the creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set the creation date
     *
     * @param creationDate the creation date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Get the target user
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the target user
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get the notification type
     *
     * @return the notification type
     */
    public ENotifType getType() {
        return type;
    }

    /**
     * Set the notification type
     *
     * @param type the notification type
     */
    public void setType(ENotifType type) {
        this.type = type;
    }

    /**
     * Get the notification status
     *
     * @return the status
     */
    public ENotifStatus getStatus() {
        return status;
    }

    /**
     * Set the notification status
     *
     * @param status the status
     */
    public void setStatus(ENotifStatus status) {
        this.status = status;
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.status = ENotifStatus.READ;
    }

    /**
     * Hide the notification
     */
    public void hide() {
        this.status = ENotifStatus.HIDDEN;
    }

    /**
     * Check if notification is unread
     *
     * @return true if status is TO_READ
     */
    public boolean isUnread() {
        return this.status == ENotifStatus.TO_READ;
    }
}
