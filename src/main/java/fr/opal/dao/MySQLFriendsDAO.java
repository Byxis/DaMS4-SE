package fr.opal.dao;

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
    public ArrayList<User> getFriendList(int userId) throws SQLException {
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
        }
        return friends;
    }

    @Override
    public ArrayList<User> getFollowedUsers(int userId) throws SQLException {
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
        }
        return followed;
    }

    @Override
    public ArrayList<User> getFollowersList(int userId) throws SQLException {
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
        }
        return followers;
    }

    @Override
    public ArrayList<User> getBlockedUsers(int userId) throws SQLException {
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
        }
        return blocked;
    }

    @Override
    public ArrayList<User> getPendingFriendRequests(int userId) throws SQLException {
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
        }
        return requests;
    }

    @Override
    public void addFriend(int fromUserId, int toUserId) throws SQLException {
        String query = "INSERT INTO friendships (user_id1, user_id2, status, created_at) " +
                      "VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, fromUserId);
            stmt.setInt(2, toUserId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) throws SQLException {
        String query = "DELETE FROM friendships WHERE " +
                      "(user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void follow(int followerId, int followedId) throws SQLException {
        String query = "INSERT INTO follows (follower_id, followed_id, created_at) " +
                      "VALUES (?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void unfollow(int followerId, int followedId) throws SQLException {
        String query = "DELETE FROM follows WHERE follower_id = ? AND followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void block(int userId, int blockedUserId) throws SQLException {
        String query = "INSERT INTO blocks (blocker_id, blocked_id, created_at) " +
                      "VALUES (?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, blockedUserId);
            stmt.executeUpdate();
        }
        
        // Remove any existing friend requests or friendships
        removeFriend(userId, blockedUserId);
        unfollow(userId, blockedUserId);
        unfollow(blockedUserId, userId);
    }

    @Override
    public void unblock(int userId, int blockedUserId) throws SQLException {
        String query = "DELETE FROM blocks WHERE blocker_id = ? AND blocked_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, blockedUserId);
            stmt.executeUpdate();
        }
    }

    @Override
    public int getFriendCount(int userId) throws SQLException {
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
        }
        return 0;
    }

    @Override
    public int getFollowerCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM follows WHERE followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public ArrayList<User> searchUsers(String query) throws SQLException {
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
        }
        return users;
    }

    @Override
    public boolean isBlocked(int userId, int otherUserId) throws SQLException {
        String query = "SELECT COUNT(*) FROM blocks WHERE blocker_id = ? AND blocked_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, otherUserId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isFriend(int userId, int otherUserId) throws SQLException {
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
        }
        return false;
    }

    @Override
    public boolean isFollowing(int followerId, int followedId) throws SQLException {
        String query = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasPendingFriendRequest(int fromUserId, int toUserId) throws SQLException {
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
        }
        return false;
    }

    @Override
    public void acceptFriendRequest(int requesterId, int userId) throws SQLException {
        String query = "UPDATE friendships SET status = 'ACCEPTED', updated_at = CURRENT_TIMESTAMP " +
                      "WHERE user_id1 = ? AND user_id2 = ? AND status = 'PENDING'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, requesterId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}
