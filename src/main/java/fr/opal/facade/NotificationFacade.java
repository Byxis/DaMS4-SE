package fr.opal.facade;

import fr.opal.exception.NotificationException;
import fr.opal.manager.NotificationManager;
import fr.opal.type.ENotifType;
import fr.opal.type.Notification;
import fr.opal.type.User;

import java.util.List;

/**
 * Facade for notification management operations
 * Thin wrapper that delegates all operations to NotificationManager
 */
public class NotificationFacade {

    private static NotificationFacade instance;
    private NotificationManager notificationManager;

    /**
     * Get singleton instance
     *
     * @return The NotificationFacade instance
     */
    public static NotificationFacade getInstance() {
        if (instance == null) {
            instance = new NotificationFacade();
        }
        return instance;
    }

    /**
     * Private constructor
     */
    private NotificationFacade() {
        this.notificationManager = NotificationManager.getInstance();
    }

    /**
     * Lists all notifications for a user - delegates to NotificationManager
     *
     * @param userId the user ID
     * @return list of notifications
     */
    public List<Notification> listNotifications(int userId) {
        return notificationManager.listNotifications(userId);
    }

    /**
     * Lists unread notifications for a user - delegates to NotificationManager
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    public List<Notification> listUnreadNotifications(int userId) {
        return notificationManager.listUnreadNotifications(userId);
    }

    /**
     * Marks a notification as read - delegates to NotificationManager
     *
     * @param notificationId the notification ID
     * @throws NotificationException if notification not found
     */
    public void markAsRead(int notificationId) throws NotificationException {
        notificationManager.markAsRead(notificationId);
    }

    /**
     * Hides a notification - delegates to NotificationManager
     *
     * @param notificationId the notification ID
     * @throws NotificationException if notification not found
     */
    public void hideNotification(int notificationId) throws NotificationException {
        notificationManager.hideNotification(notificationId);
    }

    /**
     * Deletes a notification - delegates to NotificationManager
     *
     * @param notificationId the notification ID
     * @throws NotificationException if notification not found
     */
    public void deleteNotification(int notificationId) throws NotificationException {
        notificationManager.deleteNotification(notificationId);
    }

    /**
     * Marks all notifications as read for a user - delegates to NotificationManager
     *
     * @param userId the user ID
     */
    public void markAllAsRead(int userId) {
        notificationManager.markAllAsRead(userId);
    }

    /**
     * Gets count of unread notifications - delegates to NotificationManager
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    public int getUnreadCount(int userId) {
        return notificationManager.getUnreadCount(userId);
    }

    /**
     * Sends a notification to a user - delegates to NotificationManager
     *
     * @param target the target user
     * @param type the notification type
     * @throws NotificationException if target is null
     */
    public void sendNotification(User target, ENotifType type) throws NotificationException {
        notificationManager.sendNotification(target, type);
    }

    /**
     * Sends a notification with custom content - delegates to NotificationManager
     *
     * @param target the target user
     * @param type the notification type
     * @param content the notification content
     * @throws NotificationException if target is null
     */
    public void sendNotification(User target, ENotifType type, String content) throws NotificationException {
        notificationManager.sendNotification(target, type, content);
    }

    /**
     * Gets a notification by ID - delegates to NotificationManager
     *
     * @param notificationId the notification ID
     * @return the notification or null
     */
    public Notification getNotification(int notificationId) {
        return notificationManager.getNotification(notificationId);
    }

    /**
     * Cleans up expired notifications - delegates to NotificationManager
     *
     * @param daysOld number of days after which notifications expire
     */
    public void cleanupExpiredNotifications(int daysOld) {
        notificationManager.cleanupExpiredNotifications(daysOld);
    }
}
