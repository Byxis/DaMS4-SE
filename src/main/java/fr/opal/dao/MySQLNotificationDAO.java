package fr.opal.dao;

import fr.opal.exception.NotificationException;
import fr.opal.type.ENotifStatus;
import fr.opal.type.ENotifType;
import fr.opal.type.Notification;
import fr.opal.type.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of NotificationDAO
 * Implements all abstract methods from NotificationDAO
 */
public class MySQLNotificationDAO extends NotificationDAO {

    private Connection conn;

    /**
     * Constructor with database connection
     *
     * @param conn the database connection
     */
    public MySQLNotificationDAO(Connection conn) {
        super();
        this.conn = conn;
    }

    /**
     * Creates a new notification in the database
     *
     * @param notification the notification to create
     * @return the generated notification ID
     */
    @Override
    public int createNotification(Notification notification) {
        String sql = "INSERT INTO notifications(user_id, content, type, status, creation_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUser().getId());
            ps.setString(2, notification.getContent());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getStatus().name());
            ps.setTimestamp(5, new Timestamp(notification.getCreationDate().getTime()));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new NotificationException("Error creating notification", e);
        }
        return -1;
    }

    /**
     * Updates the status of a notification
     *
     * @param notificationId the notification ID
     * @param status the new status
     */
    @Override
    public void updateStatus(int notificationId, ENotifStatus status) {
        String sql = "UPDATE notifications SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new NotificationException("Error updating notification status: " + notificationId, e);
        }
    }

    /**
     * Retrieves all notifications for a specific user
     *
     * @param userId the user ID
     * @return list of notifications for the user
     */
    @Override
    public List<Notification> getByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT n.id, n.content, n.type, n.status, n.creation_date, " +
                     "u.id as user_id, u.username, u.password " +
                     "FROM notifications n " +
                     "JOIN users u ON n.user_id = u.id " +
                     "WHERE n.user_id = ? " +
                     "ORDER BY n.creation_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(buildNotificationFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new NotificationException("Error retrieving notifications for user: " + userId, e);
        }
        return notifications;
    }

    /**
     * Finds a recent similar notification to prevent duplicates
     * Checks for notifications of the same type within the last minute
     *
     * @param userId the user ID
     * @param type the notification type
     * @return the similar notification if found, null otherwise
     */
    @Override
    public Notification findRecentSimilar(int userId, ENotifType type) {
        String sql = "SELECT n.id, n.content, n.type, n.status, n.creation_date, " +
                     "u.id as user_id, u.username, u.password " +
                     "FROM notifications n " +
                     "JOIN users u ON n.user_id = u.id " +
                     "WHERE n.user_id = ? AND n.type = ? " +
                     "AND n.creation_date > DATE_SUB(NOW(), INTERVAL 1 MINUTE) " +
                     "ORDER BY n.creation_date DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildNotificationFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new NotificationException("Error finding recent similar notification", e);
        }
        return null;
    }

    /**
     * Deletes a notification from the database
     *
     * @param notificationId the notification ID
     */
    @Override
    public void deleteNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new NotificationException("Error deleting notification: " + notificationId, e);
        }
    }

    /**
     * Retrieves a notification by its ID
     *
     * @param notificationId the notification ID
     * @return the notification or null if not found
     */
    @Override
    public Notification getById(int notificationId) {
        String sql = "SELECT n.id, n.content, n.type, n.status, n.creation_date, " +
                     "u.id as user_id, u.username, u.password " +
                     "FROM notifications n " +
                     "JOIN users u ON n.user_id = u.id " +
                     "WHERE n.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildNotificationFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new NotificationException("Error retrieving notification: " + notificationId, e);
        }
        return null;
    }

    /**
     * Retrieves unread notifications for a user
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    @Override
    public List<Notification> getUnreadByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT n.id, n.content, n.type, n.status, n.creation_date, " +
                     "u.id as user_id, u.username, u.password " +
                     "FROM notifications n " +
                     "JOIN users u ON n.user_id = u.id " +
                     "WHERE n.user_id = ? AND n.status = 'TO_READ' " +
                     "ORDER BY n.creation_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(buildNotificationFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new NotificationException("Error retrieving unread notifications for user: " + userId, e);
        }
        return notifications;
    }

    /**
     * Marks all notifications as read for a user
     *
     * @param userId the user ID
     */
    @Override
    public void markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET status = 'READ' WHERE user_id = ? AND status = 'TO_READ'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new NotificationException("Error marking all notifications as read for user: " + userId, e);
        }
    }

    /**
     * Deletes expired notifications (older than specified days)
     *
     * @param daysOld number of days after which notifications expire
     */
    @Override
    public void deleteExpiredNotifications(int daysOld) {
        String sql = "DELETE FROM notifications WHERE creation_date < DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, daysOld);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new NotificationException("Error deleting expired notifications", e);
        }
    }

    /**
     * Counts unread notifications for a user
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    @Override
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND status = 'TO_READ'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new NotificationException("Error counting unread notifications for user: " + userId, e);
        }
        return 0;
    }

    /**
     * Builds a Notification object from a ResultSet
     *
     * @param rs the ResultSet
     * @return the Notification object
     * @throws SQLException if a database access error occurs
     */
    private Notification buildNotificationFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password")
        );

        ENotifType type = ENotifType.GENERAL;
        try {
            type = ENotifType.valueOf(rs.getString("type"));
        } catch (Exception ignored) {}

        ENotifStatus status = ENotifStatus.TO_READ;
        try {
            status = ENotifStatus.valueOf(rs.getString("status"));
        } catch (Exception ignored) {}

        return new Notification(
            rs.getInt("id"),
            rs.getString("content"),
            rs.getTimestamp("creation_date"),
            user,
            type,
            status
        );
    }
}
