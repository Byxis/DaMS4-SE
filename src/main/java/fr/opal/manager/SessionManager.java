package fr.opal.manager;

import fr.opal.dao.SessionDAO;
import fr.opal.factory.SessionDAOFactory;
import fr.opal.type.SessionSettings;
import fr.opal.type.StyleColor;
import fr.opal.type.StylePalette;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Manager for session visual preferences (Singleton)
 * Handles theme application and persistence
 */
public class SessionManager {

    private static SessionManager instance;
    private SessionSettings userSessionSettings;
    private int currentUserId = -1;

    /**
     * Private constructor for singleton
     */
    private SessionManager() {
        this.userSessionSettings = new SessionSettings();
    }

    /**
     * Get singleton instance
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Load settings for a specific user
     * @param userId The user ID
     */
    public void loadSettingsForUser(int userId) {
        this.currentUserId = userId;
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        this.userSessionSettings = dao.getSessionSettings(userId);
        if (userSessionSettings == null) {
            this.userSessionSettings = new SessionSettings();
        }
    }

    /**
     * Get current session settings
     * @return The current SessionSettings
     */
    public SessionSettings getUserSessionSettings() {
        return userSessionSettings;
    }

    /**
     * Save font size preference
     * @param fontSize The font size to save
     */
    public void saveFontSize(int fontSize) {
        if (currentUserId <= 0) return;
        userSessionSettings.setFontSize(fontSize);
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        dao.saveFontSize(currentUserId, fontSize);
    }

    /**
     * Get font size for user
     * @param userId The user ID
     * @return The font size
     */
    public int getFontSize(int userId) {
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        return dao.getFontSize(userId);
    }

    /**
     * Save style palette preference
     * @param stylePalette The palette to save
     */
    public void saveStylePalette(StylePalette stylePalette) {
        if (currentUserId <= 0) return;
        userSessionSettings.setStylePalette(stylePalette);
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        dao.saveStylePalette(currentUserId, stylePalette);
    }

    /**
     * Get style palette for user
     * @param userId The user ID
     * @return The style palette
     */
    public StylePalette getStylePalette(int userId) {
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        return dao.getStylePalette(userId);
    }

    /**
     * Save accent color preference
     * @param accentColor The accent color to save
     */
    public void saveAccentColor(StyleColor accentColor) {
        if (currentUserId <= 0) return;
        userSessionSettings.setAccentColor(accentColor);
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        dao.saveAccentColor(currentUserId, accentColor);
    }

    /**
     * Get accent color for user
     * @param userId The user ID
     * @return The accent color
     */
    public StyleColor getAccentColor(int userId) {
        SessionDAO dao = SessionDAOFactory.getInstance().createSessionDAO();
        return dao.getAccentColor(userId);
    }

    /**
     * Apply current theme to a scene
     * @param scene The scene to style
     */
    public void applyTheme(Scene scene) {
        if (scene == null) return;
        
        Parent root = scene.getRoot();
        applyTheme(root);
    }

    /**
     * Apply current theme to a root node
     * @param root The root node to style
     */
    public void applyTheme(Parent root) {
        if (root == null) return;

        root.getStyleClass().removeAll("light", "dark");
        
        String paletteClass = userSessionSettings.getStylePalette().getCssClass();
        root.getStyleClass().add(paletteClass);

        String accentColor = userSessionSettings.getAccentColor().getHexCode();
        String currentStyle = root.getStyle() != null ? root.getStyle() : "";
        
        currentStyle = currentStyle.replaceAll("-fx-accent-color:\\s*#[0-9A-Fa-f]+;?", "")
                                   .replaceAll("-fx-accent:\\s*#[0-9A-Fa-f]+;?", "")
                                   .replaceAll("-fx-focus-color:\\s*#[0-9A-Fa-f]+;?", "")
                                   .replaceAll("-fx-faint-focus-color:\\s*#[0-9A-Fa-f]+;?", "");
                                   
        String newStyle = currentStyle + 
                          "-fx-accent-color: " + accentColor + "; " +
                          "-fx-accent: " + accentColor + "; " +
                          "-fx-focus-color: " + accentColor + "; " +
                          "-fx-faint-focus-color: transparent;";
                          
        root.setStyle(newStyle);
    }

    /**
     * Get current user ID
     * @return The current user ID
     */
    public int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Clear settings (on logout)
     */
    public void clearSettings() {
        this.currentUserId = -1;
        this.userSessionSettings = new SessionSettings();
    }
}
