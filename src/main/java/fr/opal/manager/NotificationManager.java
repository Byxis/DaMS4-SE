package fr.opal.manager;

import fr.opal.dao.NotificationDAO;
import fr.opal.exception.NotificationException;
import fr.opal.factory.AbstractDAOFactory;
import fr.opal.type.ENotifStatus;
import fr.opal.type.ENotifType;
import fr.opal.type.Notification;
import fr.opal.type.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for notification operations (Singleton)
 * Implements Observer pattern - receives updates from Observable sources
 * Contains notification-related business logic and persistence coordination
 */
public class NotificationManager {

    private static NotificationManager instance;
    private NotificationDAO notificationDAO;
    private List<Notification> cachedNotifications;

    /**
     * Private constructor for singleton
     */
    private NotificationManager() {
        this.notificationDAO = AbstractDAOFactory.getFactory().createNotificationDAO();
        this.cachedNotifications = new ArrayList<>();
    }

    /**
     * Get singleton instance
     *
     * @return The NotificationManager instance
     */
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Observer pattern update method
     * Called when an observable event occurs
     *
     * @param eventType the type of event
     * @param data additional event data
     */
    public void update(String eventType, Object data) {
        // Handle different event types
        switch (eventType) {
            case "PROJECT_INVITATION":
                if (data instanceof User) {
                    sendNotification((User) data, ENotifType.INVITATION);
                }
                break;
            case "NEW_COMMENT":
                if (data instanceof User) {
                    sendNotification((User) data, ENotifType.COMMENT);
                }
                break;
            case "PROJECT_UPDATE":
                if (data instanceof User) {
                    sendNotification((User) data, ENotifType.PROJECT);
                }
                break;
            case "NEW_FOLLOWER":
                if (data instanceof User) {
                    sendNotification((User) data, ENotifType.SOCIAL);
                }
                break;
            default:
                // General notification
                if (data instanceof User) {
                    sendNotification((User) data, ENotifType.GENERAL);
                }
                break;
        }
    }

    /**
     * Sends a notification to a target user
     * Handles aggregation of similar notifications
     *
     * @param target the target user
     * @param type the notification type
     */
    public void sendNotification(User target, ENotifType type) {
        sendNotification(target, type, generateDefaultContent(type));
    }

    /**
     * Sends a notification with custom content to a target user
     *
     * @param target the target user
     * @param type the notification type
     * @param content the notification content
     */
    public void sendNotification(User target, ENotifType type, String content) {
        if (target == null) {
            throw new NotificationException("Error sending notification: Target user cannot be null");
        }

        // Check for redundant notifications (aggregation)
        Notification existingSimilar = notificationDAO.findRecentSimilar(target.getId(), type);
        if (existingSimilar != null) {
            // Aggregate: update existing notification content
            String aggregatedContent = aggregateContent(existingSimilar.getContent(), content);
            existingSimilar.setContent(aggregatedContent);
            notificationDAO.updateStatus(existingSimilar.getId(), ENotifStatus.TO_READ);
            return;
        }

        // Create new notification
        Notification notification = new Notification(content, target, type);
        int notificationId = notificationDAO.createNotification(notification);
        notification.setId(notificationId);
        cachedNotifications.add(notification);
    }

    /**
     * Lists all notifications for a user
     *
     * @param userId the user ID
     * @return list of notifications
     */
    public List<Notification> listNotifications(int userId) {
        return notificationDAO.getByUser(userId);
    }

    /**
     * Lists unread notifications for a user
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    public List<Notification> listUnreadNotifications(int userId) {
        return notificationDAO.getUnreadByUser(userId);
    }

    /**
     * Marks a notification as read
     *
     * @param notificationId the notification ID
     */
    public void markAsRead(int notificationId) {
        Notification notification = notificationDAO.getById(notificationId);
        if (notification == null) {
            throw new NotificationException("Error marking notification as read: Notification not found");
        }
        notificationDAO.updateStatus(notificationId, ENotifStatus.READ);
    }

    /**
     * Hides a notification
     *
     * @param notificationId the notification ID
     */
    public void hideNotification(int notificationId) {
        Notification notification = notificationDAO.getById(notificationId);
        if (notification == null) {
            throw new NotificationException("Error hiding notification: Notification not found");
        }
        notificationDAO.updateStatus(notificationId, ENotifStatus.HIDDEN);
    }

    /**
     * Deletes a notification permanently
     *
     * @param notificationId the notification ID
     */
    public void deleteNotification(int notificationId) {
        Notification notification = notificationDAO.getById(notificationId);
        if (notification == null) {
            throw new NotificationException("Error deleting notification: Notification not found");
        }
        notificationDAO.deleteNotification(notificationId);
        cachedNotifications.removeIf(n -> n.getId() == notificationId);
    }

    /**
     * Marks all notifications as read for a user
     *
     * @param userId the user ID
     */
    public void markAllAsRead(int userId) {
        notificationDAO.markAllAsRead(userId);
    }

    /**
     * Gets count of unread notifications for a user
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    public int getUnreadCount(int userId) {
        return notificationDAO.countUnread(userId);
    }

    /**
     * Cleans up expired notifications
     *
     * @param daysOld number of days after which notifications expire
     */
    public void cleanupExpiredNotifications(int daysOld) {
        notificationDAO.deleteExpiredNotifications(daysOld);
    }

    /**
     * Retrieves a notification by ID
     *
     * @param notificationId the notification ID
     * @return the notification or null
     */
    public Notification getNotification(int notificationId) {
        return notificationDAO.getById(notificationId);
    }

    /**
     * Generates default content based on notification type
     *
     * @param type the notification type
     * @return default content string
     */
    private String generateDefaultContent(ENotifType type) {
        switch (type) {
            case PROJECT:
                return "A project has been updated.";
            case SOCIAL:
                return "You have a new social interaction.";
            case INVITATION:
                return "You have received a new invitation.";
            case COMMENT:
                return "Someone commented on your content.";
            case GENERAL:
            default:
                return "You have a new notification.";
        }
    }

    /**
     * Aggregates content for similar notifications
     *
     * @param existingContent the existing notification content
     * @param newContent the new content to aggregate
     * @return aggregated content
     */
    private String aggregateContent(String existingContent, String newContent) {
        // Simple aggregation: append count or combine messages
        if (existingContent.contains("(+")) {
            // Already aggregated, increment count
            int startIdx = existingContent.lastIndexOf("(+") + 2;
            int endIdx = existingContent.lastIndexOf(")");
            try {
                int count = Integer.parseInt(existingContent.substring(startIdx, endIdx));
                return existingContent.substring(0, existingContent.lastIndexOf("(+")) + "(+" + (count + 1) + ")";
            } catch (NumberFormatException e) {
                return existingContent + " (+2)";
            }
        } else {
            return existingContent + " (+2)";
        }
    }
}
