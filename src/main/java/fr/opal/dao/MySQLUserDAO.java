package fr.opal.dao;

import fr.opal.exception.DataAccessException;
import fr.opal.type.User;
import fr.opal.type.Permission;
import fr.opal.type.Profile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of UserDAO
 */
public class MySQLUserDAO extends UserDAO {
    private Connection conn;

    /**
     * Default constructor
     */
    public MySQLUserDAO(Connection _conn) {
        this.conn = _conn;
    }

    /**
     * Get user by username
     */
    @Override
    public User getUserById(String username) {
        String sql = "SELECT id, username, password FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user: " + username, e);
        }
        return null;
    }

    /**
     * Create a new user
     */
    @Override
    public User createUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new User(id, username, password);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + username, e);
        }
        return null;
    }

    /**
     * Get user profile
     */
    @Override
    public Profile getProfile(int userId) {
        String sql = "SELECT user_id, display_name, bio, contact_info FROM user_profiles WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new Profile(rs.getInt("user_id"), rs.getString("display_name"), rs.getString("bio"), rs.getString("contact_info"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving profile for user: " + userId, e);
        }
        return null;
    }

    /**
     * Update user profile
     */
    @Override
    public void updateProfile(int userId, Profile profile) {
        String checkSql = "SELECT user_id FROM user_profiles WHERE user_id = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, userId);
            var rs = checkPs.executeQuery();
            
            if (rs.next()) {
                String updateSql = "UPDATE user_profiles SET display_name = ?, bio = ?, contact_info = ? WHERE user_id = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setString(1, profile.getDisplayName());
                    updatePs.setString(2, profile.getBio());
                    updatePs.setString(3, profile.getContactInfo());
                    updatePs.setInt(4, userId);
                    updatePs.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO user_profiles(user_id, display_name, bio, contact_info) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setInt(1, userId);
                    insertPs.setString(2, profile.getDisplayName());
                    insertPs.setString(3, profile.getBio());
                    insertPs.setString(4, profile.getContactInfo());
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating profile for user: " + userId, e);
        }
    }

    /**
     * Get list of permissions for a user
     */
    @Override
    public List<Permission> listPermissions(int userId) {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT id, name FROM permissions WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                Permission permission = new Permission(rs.getInt("id"), rs.getString("name"));
                permissions.add(permission);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing permissions for user: " + userId, e);
        }
        return permissions;
    }

    /**
     * Create a new permission for a user
     */
    @Override
    public Permission createPermission(int userId, String permissionName) {
        String sql = "INSERT INTO permissions(user_id, name) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, permissionName);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int permissionId = rs.getInt(1);
                    return new Permission(permissionId, permissionName);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating permission for user: " + userId, e);
        }
        return null;
    }

    /**
     * Update an existing permission
     */
    @Override
    public void updatePermission(int permissionId, String permissionName) {
        String sql = "UPDATE permissions SET name = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, permissionName);
            ps.setInt(2, permissionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating permission: " + permissionId, e);
        }
    }

    /**
     * Delete a permission
     */
    @Override
    public void deletePermission(int permissionId) {
        String sql = "DELETE FROM permissions WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, permissionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting permission: " + permissionId, e);
        }
    }
}