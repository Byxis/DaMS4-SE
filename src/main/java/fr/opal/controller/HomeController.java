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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/profile-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            dialogPane.getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            sessionPropertiesFacade.applyTheme(dialogPane);

            TextField usernameField = (TextField) dialogPane.lookup("#usernameField");
            TextField emailField = (TextField) dialogPane.lookup("#emailField");
            TextField displayNameField = (TextField) dialogPane.lookup("#displayNameField");
            TextField bioField = (TextField) dialogPane.lookup("#bioField");

            usernameField.setText(currentSession.getUsername());
            if (currentProfile != null) {
                emailField.setText(currentProfile.getContactInfo());
                displayNameField.setText(currentProfile.getDisplayName());
                bioField.setText(currentProfile.getBio());
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Opal - Profile");

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    saveProfileChanges(
                            displayNameField.getText(),
                            bioField.getText(),
                            emailField.getText()
                    );
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            if (currentSession != null) {
                sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            }

            SessionPropertiesController settingsController = new SessionPropertiesController();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/session-properties-view.fxml"));
            loader.setController(settingsController);
            Parent settingsContent = loader.load();
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Appearance Settings");

            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            sessionPropertiesFacade.applyTheme(dialog.getDialogPane());
            dialog.getDialogPane().setContent(settingsContent);
            sessionPropertiesFacade.applyTheme(settingsContent);

            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            Runnable updateCloseButtonColor = () -> {
                String accentColor = sessionPropertiesFacade.getSettings().getAccentColor().getHexCode();
                String textColor = ColorUtil.getContrastTextColor(accentColor);
                javafx.application.Platform.runLater(() -> {
                    dialog.getDialogPane().lookupAll(".button").forEach(node -> {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            if (!btn.getStyleClass().contains("color-button")) {
                                btn.setStyle(
                                    "-fx-background-color: " + accentColor + "; " +
                                    "-fx-text-fill: " + textColor + "; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-padding: 8px 16px;"
                                );
                            }
                        }
                    });
                });
            };

            settingsController.initialize();
            updateCloseButtonColor.run();

            settingsController.setOnSettingsChanged(() -> {
                applyCurrentTheme();
                updateCloseButtonColor.run();
            });

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
