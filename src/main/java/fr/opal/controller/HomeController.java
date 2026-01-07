package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.service.SceneManager;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.util.ColorUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * Controller for the home screen
 */
public class HomeController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button userProfileBtn;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button settingsBtn;
    @FXML
    private Label welcomeLabel;

    private AuthFacade authFacade;
    private SessionPropertiesFacade sessionPropertiesFacade;
    private SceneManager sceneManager;
    private Session currentSession;
    private Profile currentProfile;

    public HomeController() {
        this.authFacade = AuthFacade.getInstance();
        this.sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
        if (currentSession != null) {
            usernameLabel.setText(currentSession.getUsername());
            currentProfile = authFacade.getProfile(currentSession.getUserId());
            sessionPropertiesFacade.loadSettings(currentSession.getUserId());

            
            String welcomeText = "Welcome, " + currentSession.getUsername() + "!";
            if (currentProfile != null && currentProfile.getDisplayName() != null) {
                welcomeText = "Welcome, " + currentProfile.getDisplayName() + "!";
            }
            welcomeLabel.setText(welcomeText);

            applyCurrentTheme();
        } else {
            redirectToLogin();
        }
    }

    /**
     * Apply current theme to the root pane
     */
    private void applyCurrentTheme() {
        if (rootPane != null) {
            sessionPropertiesFacade.applyTheme(rootPane);

            String accentColor = sessionPropertiesFacade.getSettings().getAccentColor().getHexCode();
            String textColor = ColorUtil.getContrastTextColor(accentColor);
            
            String buttonStyle = "-fx-background-color: " + accentColor + "; " +
                                "-fx-text-fill: " + textColor + "; " +
                                "-fx-font-weight: bold;";
            
            if (userProfileBtn != null) {
                userProfileBtn.setStyle(buttonStyle);
            }
            if (settingsBtn != null) {
                settingsBtn.setStyle(buttonStyle);
            }
            if (logoutBtn != null) {
                logoutBtn.setStyle(buttonStyle);
            }
        }
    }

    /**
     * Opens a dialog to view and edit user profile
     */
    @FXML
    private void openProfileDialog() {
        sceneManager.openProfileDialog(currentSession, currentProfile, sessionPropertiesFacade, (displayName, bio, email) -> {
            saveProfileChanges(displayName, bio, email);
        });
    }

    /**
     * Saves profile changes
     */
    private void saveProfileChanges(String firstName, String lastName, String email) {
        try {
            if (currentSession != null) {
                Profile profile = new Profile(currentSession.getUserId(), firstName, lastName, email);
                authFacade.updateProfile(currentSession.getUserId(), profile);
                currentProfile = profile;
                refreshWelcomeMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the welcome message with updated profile data
     */
    private void refreshWelcomeMessage() {
        String welcomeText = "Welcome, " + currentSession.getUsername() + "!";
        if (currentProfile != null && currentProfile.getDisplayName() != null && !currentProfile.getDisplayName().isEmpty()) {
            String firstName = currentProfile.getDisplayName();
            String lastName = currentProfile.getBio() != null ? currentProfile.getBio() : "";
            if (!lastName.isEmpty()) {
                welcomeText = "Welcome, " + firstName + " " + lastName + "!";
            } else {
                welcomeText = "Welcome, " + firstName + "!";
            }
        }
        welcomeLabel.setText(welcomeText);
    }

    /**
     * Handles user logout
     */
    @FXML
    private void logout() {
        if (currentSession != null) {
            authFacade.logout(currentSession.getId());
            sessionPropertiesFacade.clearSettings();
        }
        redirectToLogin();
    }

    /**
     * Opens the session settings dialog
     */
    @FXML
    private void openSettings() {
        sceneManager.openSettingsDialog(currentSession, sessionPropertiesFacade, this::applyCurrentTheme);
    }

    /**
     * Redirects to login screen
     */
    private void redirectToLogin() {
        try {
            AuthController.clearMessageLabel();
            sceneManager.switchTo("/fr/opal/login-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the friends list view
     */
    @FXML
    private void openFriendsList() {
        try {
            sceneManager.openNewWindow("/fr/opal/friend-list-view.fxml", "Friends - Opal", 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the friend search view
     */
    @FXML
    private void openFriendSearch() {
        try {
            sceneManager.openNewWindow("/fr/opal/friend-search-view.fxml", "Find Friends - Opal", 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
