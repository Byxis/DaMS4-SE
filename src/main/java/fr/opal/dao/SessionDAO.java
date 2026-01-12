package fr.opal.dao;

import fr.opal.type.SessionSettings;
import fr.opal.type.StyleColor;
import fr.opal.type.StylePalette;

/**
 * Abstract DAO for session preferences operations
 */
public abstract class SessionDAO {

    /**
     * Get session settings for a user
     * @param userId The user ID
     * @return SessionSettings or null if not found
     */
    public abstract SessionSettings getSessionSettings(int userId);

    /**
     * Save font size preference
     * @param userId The user ID
     * @param fontSize The font size to save
     */
    public abstract void saveFontSize(int userId, int fontSize);

    /**
     * Get font size for a user
     * @param userId The user ID
     * @return The font size or default value
     */
    public abstract int getFontSize(int userId);

    /**
     * Save style palette preference
     * @param userId The user ID
     * @param stylePalette The style palette to save
     */
    public abstract void saveStylePalette(int userId, StylePalette stylePalette);

    /**
     * Get style palette for a user
     * @param userId The user ID
     * @return The style palette or default value
     */
    public abstract StylePalette getStylePalette(int userId);

    /**
     * Save accent color preference
     * @param userId The user ID
     * @param accentColor The accent color to save
     */
    public abstract void saveAccentColor(int userId, StyleColor accentColor);

    /**
     * Get accent color for a user
     * @param userId The user ID
     * @return The accent color or default value
     */
    public abstract StyleColor getAccentColor(int userId);

    /**
     * Save all session settings at once
     * @param userId The user ID
     * @param settings The session settings to save
     */
    public abstract void saveSessionSettings(int userId, SessionSettings settings);
}
