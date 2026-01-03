package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.service.SceneManager;
import fr.opal.type.Permission;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.type.User;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.List;

/**
 * Controller for authentication operations including login and registration
 */
public class AuthController {
    
    private static String pendingMessage = "";

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    private AuthFacade authFacade;
    private SceneManager sceneManager;
    private Session currentSession;

    public AuthController() {
        this.authFacade = AuthFacade.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        if (!pendingMessage.isEmpty()) {
            messageLabel.setText(pendingMessage);
            pendingMessage = "";
        }
    }

    /**
     * Handles user login (FXML form)
     */
    @FXML
    private void submitLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty");
            return;
        }

        currentSession = authFacade.login(username, password);
        
        if (currentSession != null) {
            pendingMessage = "";
            clearFields();
            switchToHome();
        } else {
            messageLabel.setText("Invalid username or password");
        }
    }

    /**
     * Handles user registration
     */
    @FXML
    private void register() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty()) {
            messageLabel.setText("Username cannot be empty");
            return;
        }

        if (password.isEmpty()) {
            messageLabel.setText("Password cannot be empty");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters");
            return;
        }

        User newUser = authFacade.register(username, password);
        
        if (newUser != null) {
            pendingMessage = "Registration successful! Please log in.";
            clearFields();
            switchToLogin();
        } else {
            messageLabel.setText("Registration failed! User may already exist.");
        }
    }

    /**
     * Navigates to registration screen
     */
    @FXML
    private void goToRegister() {
        try {
            pendingMessage = "";
            sceneManager.switchTo("/fr/opal/register-view.fxml");
        } catch (IOException e) {
            messageLabel.setText("Error loading registration screen");
        }
    }

    /**
     * Navigates to home screen
     */
    private void switchToHome() {
        try {
            sceneManager.switchTo("/fr/opal/home.fxml");
        } catch (IOException e) {
            messageLabel.setText("Error loading home screen");
        }
    }

    /**
     * Clears all input fields
     */
    private void clearFields() {
        if (usernameField != null) usernameField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
    }

    /**
     * Returns to login screen
     */
    @FXML
    private void switchToLogin() {
        try {
            sceneManager.switchTo("/fr/opal/login-view.fxml");
        } catch (IOException e) {
            messageLabel.setText("Error loading login screen");
        }
    }

    /**
     * Clears the pending message (static method for external use)
     */
    public static void clearMessageLabel() {
        pendingMessage = "";
    }

    /**
     * Handles user logout
     */
    @FXML
    private void logout() {
        if (currentSession != null) {
            authFacade.logout(currentSession.getId());
            currentSession = null;
        }
    }

    /**
     * Opens user profile
     */
    @FXML
    private void openProfile() {
        if (currentSession != null) {
            System.out.println("Opening profile for session: " + currentSession.getId());
        }
    }

    /**
     * Updates user profile
     */
    @FXML
    public void updateProfile(int userId, Profile profile) {
        authFacade.updateProfile(userId, profile);
    }

    /**
     * Creates a new permission for user
     */
    @FXML
    public Permission createPermission(int userId, String permissionName) {
        return authFacade.createPermission(userId, permissionName);
    }

    /**
     * Deletes a permission
     */
    @FXML
    public void deletePermission(int permissionId) {
        authFacade.deletePermission(permissionId);
    }

    /**
     * Updates a permission
     */
    @FXML
    public void updatePermission(int permissionId, String permissionName) {
        authFacade.updatePermission(permissionId, permissionName);
    }

    /**
     * Lists all permissions for a user
     */
    @FXML
    public List<Permission> listPermissions(int userId) {
        return authFacade.listPermissions(userId);
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session session) {
        this.currentSession = session;
    }
}
