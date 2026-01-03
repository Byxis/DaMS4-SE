package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.service.SceneManager;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controller for the home screen
 */
public class HomeController {

    @FXML
    private Button userProfileBtn;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutBtn;
    @FXML
    private Label welcomeLabel;

    private AuthFacade authFacade;
    private SceneManager sceneManager;
    private Session currentSession;
    private Profile currentProfile;

    public HomeController() {
        this.authFacade = AuthFacade.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
        if (currentSession != null) {
            usernameLabel.setText(currentSession.getUsername());
            currentProfile = authFacade.getProfile(currentSession.getUserId());
            
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
     * Opens a dialog to view and edit user profile
     */
    @FXML
    private void openProfileDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/profile-dialog.fxml"));
            DialogPane dialogPane = loader.load();

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
        }
        redirectToLogin();
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
