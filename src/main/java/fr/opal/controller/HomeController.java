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
            
            // Load and apply user session settings
            sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            
            // Apply theme after a short delay to ensure scene is ready
            javafx.application.Platform.runLater(this::applyCurrentTheme);
            
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
            
            // Style navbar buttons with accent color and appropriate text color
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

            // Add stylesheet and apply theme
            dialogPane.getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            sessionPropertiesFacade.applyTheme(dialogPane);

            TextField usernameField = (TextField) dialogPane.lookup("#usernameField");
            TextField emailField = (TextField) dialogPane.lookup("#emailField");
            TextField firstNameField = (TextField) dialogPane.lookup("#firstNameField");
            TextField lastNameField = (TextField) dialogPane.lookup("#lastNameField");

            usernameField.setText(currentSession.getUsername());
            if (currentProfile != null) {
                emailField.setText(currentProfile.getContactInfo());
                firstNameField.setText(currentProfile.getDisplayName());
                lastNameField.setText(currentProfile.getBio());
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Opal - Profile");

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    saveProfileChanges(
                            firstNameField.getText(),
                            lastNameField.getText(),
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
            // FORCE reload settings from database before opening dialog
            if (currentSession != null) {
                sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            }
            
            // Create controller BEFORE loading FXML
            SessionPropertiesController settingsController = new SessionPropertiesController();
            
            // Create FXMLLoader and set controller manually
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/session-properties-view.fxml"));
            loader.setController(settingsController);
            Parent settingsContent = loader.load();
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Appearance Settings");
            
            // Add stylesheet first
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            
            // Apply theme to dialog first
            sessionPropertiesFacade.applyTheme(dialog.getDialogPane());
            
            // Set content after theme is applied
            dialog.getDialogPane().setContent(settingsContent);
            sessionPropertiesFacade.applyTheme(settingsContent);
            
            // Add button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Function to update close button color
            Runnable updateCloseButtonColor = () -> {
                String accentColor = sessionPropertiesFacade.getSettings().getAccentColor().getHexCode();
                String textColor = ColorUtil.getContrastTextColor(accentColor);
                javafx.application.Platform.runLater(() -> {
                    // Find all buttons in the DialogPane (using CSS selector)
                    dialog.getDialogPane().lookupAll(".button").forEach(node -> {
                        if (node instanceof javafx.scene.control.Button) {
                            javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
                            // Check if this is not a color button (only style dialog buttons)
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
            
            // Initialize the controller AFTER loading the FXML
            settingsController.initialize();
            
            // Update close button color initially
            updateCloseButtonColor.run();
            
            // Update close button color when settings change
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
}
