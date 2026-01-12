package fr.opal.dao;

import fr.opal.exception.DataAccessException;
import fr.opal.type.User;
import java.sql.*;
import java.util.ArrayList;

/**
 * MySQL implementation of FriendsDAO for managing friend relationships.
 */
public class MySQLFriendsDAO extends FriendsDAO {

    private final Connection connection;

    public MySQLFriendsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ArrayList<User> getFriendList(int userId) {
        ArrayList<User> friends = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.password FROM users u " +
                      "INNER JOIN friendships f ON (u.id = f.user_id1 OR u.id = f.user_id2) " +
                      "WHERE (f.user_id1 = ? OR f.user_id2 = ?) AND u.id != ? AND f.status = 'ACCEPTED'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    friends.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting friend list for user: " + userId, e);
        }
        return friends;
    }

    @Override
    public ArrayList<User> getFollowedUsers(int userId) {
        ArrayList<User> followed = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.password FROM users u " +
                      "INNER JOIN follows f ON u.id = f.followed_id " +
                      "WHERE f.follower_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    followed.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
             throw new DataAccessException("Error getting followed users for user: " + userId, e);
        }
        return followed;
    }

    @Override
    public ArrayList<User> getFollowersList(int userId) {
        ArrayList<User> followers = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.password FROM users u " +
                      "INNER JOIN follows f ON u.id = f.follower_id " +
                      "WHERE f.followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    followers.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting followers list for user: " + userId, e);
        }
        return followers;
    }

    @Override
    public ArrayList<User> getBlockedUsers(int userId) {
        ArrayList<User> blocked = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.password FROM users u " +
                      "INNER JOIN blocks b ON u.id = b.blocked_id " +
                      "WHERE b.blocker_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    blocked.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting blocked users for user: " + userId, e);
        }
        return blocked;
    }

    @Override
    public ArrayList<User> getPendingFriendRequests(int userId) {
        ArrayList<User> requests = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.password FROM users u " +
                      "INNER JOIN friendships f ON u.id = f.user_id1 " +
                      "WHERE f.user_id2 = ? AND f.status = 'PENDING'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting pending friend requests for user: " + userId, e);
        }
        return requests;
    }

    @Override
    public void addFriend(int fromUserId, int toUserId) {
        String query = "INSERT INTO friendships (user_id1, user_id2, status, created_at) " +
                      "VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, fromUserId);
            stmt.setInt(2, toUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error adding friend request from " + fromUserId + " to " + toUserId, e);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String query = "DELETE FROM friendships WHERE " +
                      "(user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error removing friend relationship between " + userId + " and " + friendId, e);
        }
    }

    @Override
    public void follow(int followerId, int followedId) {
        String query = "INSERT INTO follows (follower_id, followed_id, created_at) " +
                      "VALUES (?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error following user " + followedId + " by " + followerId, e);
        }
    }

    @Override
    public void unfollow(int followerId, int followedId) {
        String query = "DELETE FROM follows WHERE follower_id = ? AND followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error unfollowing user " + followedId + " by " + followerId, e);
        }
    }

    @Override
    public void block(int userId, int blockedUserId) {
        String query = "INSERT INTO blocks (blocker_id, blocked_id, created_at) " +
                      "VALUES (?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, blockedUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
             throw new DataAccessException("Error blocking user " + blockedUserId + " by " + userId, e);
        }
        
        // Remove any existing friend requests or friendships
        removeFriend(userId, blockedUserId);
        unfollow(userId, blockedUserId);
        unfollow(blockedUserId, userId);
    }

    @Override
    public void unblock(int userId, int blockedUserId) {
        String query = "DELETE FROM blocks WHERE blocker_id = ? AND blocked_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, blockedUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error unblocking user " + blockedUserId + " by " + userId, e);
        }
    }

    @Override
    public int getFriendCount(int userId) {
        String query = "SELECT COUNT(*) FROM friendships WHERE " +
                      "(user_id1 = ? OR user_id2 = ?) AND status = 'ACCEPTED'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting friend count for user: " + userId, e);
        }
        return 0;
    }

    @Override
    public int getFollowerCount(int userId) {
        String query = "SELECT COUNT(*) FROM follows WHERE followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting follower count for user: " + userId, e);
        }
        return 0;
    }

    @Override
    public ArrayList<User> searchUsers(String query) {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password FROM users WHERE username LIKE ? LIMIT 50";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error searching users with query: " + query, e);
        }
        return users;
    }

    @Override
    public boolean isBlocked(int userId, int otherUserId) {
        String query = "SELECT COUNT(*) FROM blocks WHERE blocker_id = ? AND blocked_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, otherUserId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if user " + otherUserId + " is blocked by " + userId, e);
        }
        return false;
    }

    @Override
    public boolean isFriend(int userId, int otherUserId) {
        String query = "SELECT COUNT(*) FROM friendships WHERE " +
                      "((user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)) " +
                      "AND status = 'ACCEPTED'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, otherUserId);
            stmt.setInt(3, otherUserId);
            stmt.setInt(4, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking friendship between " + userId + " and " + otherUserId, e);
        }
        return false;
    }

    @Override
    public boolean isFollowing(int followerId, int followedId) {
        String query = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
             throw new DataAccessException("Error checking if " + followerId + " is following " + followedId, e);
        }
        return false;
    }

    @Override
    public boolean hasPendingFriendRequest(int fromUserId, int toUserId) {
        String query = "SELECT COUNT(*) FROM friendships WHERE " +
                      "user_id1 = ? AND user_id2 = ? AND status = 'PENDING'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, fromUserId);
            stmt.setInt(2, toUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking pending friend request from " + fromUserId + " to " + toUserId, e);
        }
        return false;
    }

    @Override
    public void acceptFriendRequest(int requesterId, int userId) {
        // First, create a channel for the DM conversation
        int channelId = createChannelForFriendship();
        
        // Then update the friendship status and assign the channel
        String query = "UPDATE friendships SET status = 'ACCEPTED', channel_id = ?, updated_at = CURRENT_TIMESTAMP " +
                      "WHERE user_id1 = ? AND user_id2 = ? AND status = 'PENDING'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, channelId);
            stmt.setInt(2, requesterId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error accepting friend request from " + requesterId + " to " + userId, e);
        }
    }

    /**
     * Creates a new channel for a friendship DM conversation.
     */
    private int createChannelForFriendship() {
        String sql = "INSERT INTO channels() VALUES()";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating channel for friendship", e);
        }
        return 0;
    }

    @Override
    public int getChannelIdForFriendship(int user1Id, int user2Id) {
        String query = "SELECT channel_id FROM friendships " +
                      "WHERE ((user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)) " +
                      "AND status = 'ACCEPTED'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int channelId = rs.getInt("channel_id");
                    if (!rs.wasNull()) {
                        return channelId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting channel ID for friendship between " + user1Id + " and " + user2Id, e);
        }
        return 0; // Channel doesn't exist or friendship not accepted
    }
}
