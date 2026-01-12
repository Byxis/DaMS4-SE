package fr.opal.dao;

import fr.opal.type.ENotifStatus;
import fr.opal.type.ENotifType;
import fr.opal.type.Notification;

import java.util.List;

/**
 * Abstract DAO for Notification persistence operations
 */
public abstract class NotificationDAO {

    /**
     * Default constructor
     */
    public NotificationDAO() {
    }

    /**
     * Creates a new notification in the database
     *
     * @param notification the notification to create
     * @return the generated notification ID
     */
    public abstract int createNotification(Notification notification);

    /**
     * Updates the status of a notification
     *
     * @param notificationId the notification ID
     * @param status the new status
     */
    public abstract void updateStatus(int notificationId, ENotifStatus status);

    /**
     * Retrieves all notifications for a specific user
     *
     * @param userId the user ID
     * @return list of notifications for the user
     */
    public abstract List<Notification> getByUser(int userId);

    /**
     * Finds a recent similar notification to prevent duplicates
     *
     * @param userId the user ID
     * @param type the notification type
     * @return the similar notification if found, null otherwise
     */
    public abstract Notification findRecentSimilar(int userId, ENotifType type);

    /**
     * Deletes a notification from the database
     *
     * @param notificationId the notification ID
     */
    public abstract void deleteNotification(int notificationId);

    /**
     * Retrieves a notification by its ID
     *
     * @param notificationId the notification ID
     * @return the notification or null if not found
     */
    public abstract Notification getById(int notificationId);

    /**
     * Retrieves unread notifications for a user
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    public abstract List<Notification> getUnreadByUser(int userId);

    /**
     * Marks all notifications as read for a user
     *
     * @param userId the user ID
     */
    public abstract void markAllAsRead(int userId);

    /**
     * Deletes expired notifications (older than specified days)
     *
     * @param daysOld number of days after which notifications expire
     */
    public abstract void deleteExpiredNotifications(int daysOld);

    /**
     * Counts unread notifications for a user
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    public abstract int countUnread(int userId);
}
