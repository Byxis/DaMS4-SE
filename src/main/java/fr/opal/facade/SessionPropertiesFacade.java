package fr.opal.facade;

import fr.opal.manager.SessionManager;
import fr.opal.type.SessionSettings;
import fr.opal.type.StyleColor;
import fr.opal.type.StylePalette;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Facade for session properties management (Singleton)
 * Provides simplified interface for controllers
 */
public class SessionPropertiesFacade {

    private static SessionPropertiesFacade instance;

    /**
     * Private constructor for singleton
     */
    private SessionPropertiesFacade() {
    }

    /**
     * Get singleton instance
     * @return The SessionPropertiesFacade instance
     */
    public static synchronized SessionPropertiesFacade getInstance() {
        if (instance == null) {
            instance = new SessionPropertiesFacade();
        }
        return instance;
    }

    /**
     * Load settings for a user
     * @param userId The user ID
     */
    public void loadSettings(int userId) {
        SessionManager.getInstance().loadSettingsForUser(userId);
    }

    /**
     * Get current session settings
     * @return The current settings
     */
    public SessionSettings getSettings() {
        return SessionManager.getInstance().getUserSessionSettings();
    }

    /**
     * Save font size preference
     * @param fontSize The font size to save
     */
    public void saveFontSize(int fontSize) {
        SessionManager.getInstance().saveFontSize(fontSize);
    }

    /**
     * Save style palette preference
     * @param stylePalette The palette to save
     */
    public void saveStylePalette(StylePalette stylePalette) {
        SessionManager.getInstance().saveStylePalette(stylePalette);
    }

    /**
     * Save accent color preference
     * @param accentColor The accent color to save
     */
    public void saveAccentColor(StyleColor accentColor) {
        SessionManager.getInstance().saveAccentColor(accentColor);
    }

    /**
     * Apply current theme to a scene
     * @param scene The scene to apply theme to
     */
    public void applyTheme(Scene scene) {
        SessionManager.getInstance().applyTheme(scene);
    }

    /**
     * Apply current theme to a root node
     * @param root The root to apply theme to
     */
    public void applyTheme(Parent root) {
        SessionManager.getInstance().applyTheme(root);
    }

    /**
     * Clear settings on logout
     */
    public void clearSettings() {
        SessionManager.getInstance().clearSettings();
    }
}
