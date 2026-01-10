package fr.opal.dao;

import fr.opal.exception.DataAccessException;
import fr.opal.type.SessionSettings;
import fr.opal.type.StyleColor;
import fr.opal.type.StylePalette;

import java.sql.*;

/**
 * MySQL implementation of SessionDAO
 */
public class MySQLSessionDAO extends SessionDAO {

    private Connection conn;

    /**
     * Constructor with connection
     */
    public MySQLSessionDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public SessionSettings getSessionSettings(int userId) {
        String sql = "SELECT font_size, style_palette, accent_color FROM session_settings WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int fontSize = rs.getInt("font_size");
                String paletteStr = rs.getString("style_palette");
                String colorStr = rs.getString("accent_color");

                StylePalette palette = StylePalette.LIGHT;
                try {
                    palette = StylePalette.valueOf(paletteStr);
                } catch (Exception ignored) {}

                StyleColor color = StyleColor.BLUE;
                try {
                    color = StyleColor.valueOf(colorStr);
                } catch (Exception ignored) {}

                return new SessionSettings(fontSize, palette, color);
            } else {
                // Create default settings for new user
                ensureSettingsExist(userId);
                return new SessionSettings();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting session settings for user: " + userId, e);
        }
    }

    @Override
    public void saveFontSize(int userId, int fontSize) {
        ensureSettingsExist(userId);
        String sql = "UPDATE session_settings SET font_size = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fontSize);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving font size for user: " + userId, e);
        }
    }

    @Override
    public int getFontSize(int userId) {
        String sql = "SELECT font_size FROM session_settings WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("font_size");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting font size for user: " + userId, e);
        }
        return 14; // default
    }

    @Override
    public void saveStylePalette(int userId, StylePalette stylePalette) {
        ensureSettingsExist(userId);
        String sql = "UPDATE session_settings SET style_palette = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stylePalette.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving style palette for user: " + userId, e);
        }
    }

    @Override
    public StylePalette getStylePalette(int userId) {
        String sql = "SELECT style_palette FROM session_settings WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String paletteStr = rs.getString("style_palette");
                try {
                    return StylePalette.valueOf(paletteStr);
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting style palette for user: " + userId, e);
        }
        return StylePalette.LIGHT;
    }

    @Override
    public void saveAccentColor(int userId, StyleColor accentColor) {
        ensureSettingsExist(userId);
        String sql = "UPDATE session_settings SET accent_color = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accentColor.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving accent color for user: " + userId, e);
        }
    }

    @Override
    public StyleColor getAccentColor(int userId) {
        String sql = "SELECT accent_color FROM session_settings WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String colorStr = rs.getString("accent_color");
                try {
                    return StyleColor.valueOf(colorStr);
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting accent color for user: " + userId, e);
        }
        return StyleColor.BLUE;
    }

    @Override
    public void saveSessionSettings(int userId, SessionSettings settings) {
        ensureSettingsExist(userId);
        String sql = "UPDATE session_settings SET font_size = ?, style_palette = ?, accent_color = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, settings.getFontSize());
            ps.setString(2, settings.getStylePalette().name());
            ps.setString(3, settings.getAccentColor().name());
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving session settings for user: " + userId, e);
        }
    }

    /**
     * Ensures that a settings row exists for the user, creating one if necessary
     */
    private void ensureSettingsExist(int userId) {
        String checkSql = "SELECT user_id FROM session_settings WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                String insertSql = "INSERT INTO session_settings (user_id, font_size, style_palette, accent_color) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setInt(1, userId);
                    insertPs.setInt(2, 14);
                    insertPs.setString(3, StylePalette.LIGHT.name());
                    insertPs.setString(4, StyleColor.BLUE.name());
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error ensuring settings exist for user: " + userId, e);
        }
    }
}
