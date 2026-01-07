package fr.opal.service;

import fr.opal.controller.SessionPropertiesController;
import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.type.CachedScene;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.util.ColorUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for handling scene transitions and window management in JavaFX.
 */
public class SceneManager {

    private static SceneManager instance;

    private Stage primaryStage;
    private final Map<String, CachedScene> cache = new HashMap<>();

    private SceneManager() {}

    /**
     * Get singleton instance
     * @return The SceneManager instance
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Initializes the SceneManager with the primary stage.
     * @param stage The primary stage
     */
    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Switches the primary stage to the specified FXML file.
     * Caches scenes for faster subsequent loading.
     *
     * @param fxmlPath The path to the FXML file
     * @throws IOException if the FXML file cannot be loaded
     */
    public void switchTo(String fxmlPath) throws IOException {
        CachedScene cached = cache.get(fxmlPath);

        if (cached == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            double width = root.prefWidth(-1);
            double height = root.prefHeight(-1);
            
            if (width <= 0) width = 600;
            if (height <= 0) height = 400;
            
            Scene scene = new Scene(root, width, height);

            cached = new CachedScene(scene, loader.getController());
            cache.put(fxmlPath, cached);
        }

        primaryStage.setScene(cached.getScene());
        primaryStage.sizeToScene();
    }

    /**
     * Opens a new window with the specified FXML file.
     *
     * @param fxmlPath The path to the FXML file
     * @param title The window title
     * @param width The window width
     * @param height The window height
     * @throws IOException if the FXML file cannot be loaded
     */
    public void openNewWindow(String fxmlPath, String title, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, width, height);
        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.setScene(scene);
        newStage.setMinWidth(600);
        newStage.setMinHeight(400);

        Image icon = new Image(getClass().getResourceAsStream("/fr/opal/opal-icon.png"));
        newStage.getIcons().add(icon);

        newStage.show();
    }

    /**
     * Loads a scene from an FXML file.
     *
     * @param fxmlPath The path to the FXML file
     * @return The loaded Scene
     * @throws IOException if the FXML file cannot be loaded
     */
    public Scene loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        double width = root.prefWidth(-1);
        double height = root.prefHeight(-1);
        
        if (width <= 0) width = 600;
        if (height <= 0) height = 400;
        
        return new Scene(root, width, height);
    }
    public void openProfileDialog(Session currentSession, Profile currentProfile, SessionPropertiesFacade sessionPropertiesFacade, ProfileDialogCallback onProfileSaved) {
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
                if (response == ButtonType.OK && onProfileSaved != null) {
                    onProfileSaved.onProfileSaved(
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

    public interface ProfileDialogCallback {
        void onProfileSaved(String displayName, String bio, String email);
    }

    public void openSettingsDialog(Session currentSession, SessionPropertiesFacade sessionPropertiesFacade, Runnable onSettingsChanged) {
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
                if (onSettingsChanged != null) onSettingsChanged.run();
                updateCloseButtonColor.run();
            });

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}