package fr.opal.dao;

import fr.opal.exception.DataAccessException;
import fr.opal.type.Channel;
import fr.opal.type.Message;
import fr.opal.type.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of ChannelDAO.
 * Part of the unified channel architecture where channels are generic
 * containers and context is derived from the owning entity (Entry or Friendship).
 */
public class MySQLChannelDAO extends ChannelDAO {
    private final Connection conn;
    private final MySQLUserDAO userDAO;

    public MySQLChannelDAO(Connection conn) {
        this.conn = conn;
        this.userDAO = new MySQLUserDAO(conn);
    }

    // ==================== Channel Operations ====================

    @Override
    public int createChannel() {
        String sql = "INSERT INTO channels() VALUES()";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating channel", e);
        }
        return 0;
    }

    @Override
    public Channel getChannelById(int id) {
        String sql = "SELECT id, created_at FROM channels WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Channel(
                        rs.getInt("id"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting channel: " + id, e);
        }
        return null;
    }

    @Override
    public void deleteChannel(int id) {
        String sql = "DELETE FROM channels WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting channel: " + id, e);
        }
    }

    // ==================== Message Operations ====================

    @Override
    public List<Message> getMessagesForChannel(int channelId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, channel_id, sender_id, content, created_at " +
                     "FROM messages WHERE channel_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(buildMessageFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting messages for channel: " + channelId, e);
        }
        return messages;
    }

    @Override
    public List<Message> getRecentMessages(int channelId, int limit) {
        List<Message> messages = new ArrayList<>();
        // Get recent messages in descending order, then reverse for display
        String sql = "SELECT id, channel_id, sender_id, content, created_at " +
                     "FROM messages WHERE channel_id = ? ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, channelId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(0, buildMessageFromResultSet(rs)); // Add at beginning to reverse order
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting recent messages for channel: " + channelId, e);
        }
        return messages;
    }

    @Override
    public long saveMessage(Message message) {
        String sql = "INSERT INTO messages(channel_id, sender_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getChannelId());
            ps.setInt(2, message.getSender().getId());
            ps.setString(3, message.getContent());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    message.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving message", e);
        }
        return 0;
    }

    @Override
    public void deleteMessage(long messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting message: " + messageId, e);
        }
    }

    @Override
    public void updateMessageContent(long messageId, String newContent) {
        String sql = "UPDATE messages SET content = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setLong(2, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating message: " + messageId, e);
        }
    }

    @Override
    public int getMessageCount(int channelId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE channel_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting message count for channel: " + channelId, e);
        }
        return 0;
    }

    /**
     * Helper method to build Message from ResultSet
     */
    private Message buildMessageFromResultSet(ResultSet rs) throws SQLException {
        int senderId = rs.getInt("sender_id");
        User sender = userDAO.getUserByDatabaseId(senderId);

        if (sender == null) {
            // Handle orphaned messages (sender deleted) - create placeholder user
            sender = new User(senderId, "[deleted]", "");
        }

        return new Message(
            rs.getLong("id"),
            rs.getInt("channel_id"),
            sender,
            rs.getString("content"),
            rs.getTimestamp("created_at")
        );
    }
}
