package fr.opal.UI.login;

import fr.opal.manager.SceneManager;
import fr.opal.db.DatabaseManager;
import fr.opal.db.DatabaseInitializer;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

/**
 * Main application class for the login interface.
 */
public class LoginApplication extends Application {

    /**
     * Starts the JavaFX application.
     *
     * @param stage The primary stage for this application
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void start(Stage stage) throws IOException {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            DatabaseInitializer.initialize(conn);
        } catch (Exception e) {
            System.err.println("Warning: Failed to update database schema: " + e.getMessage());
        }
        
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.initialize(stage);

        sceneManager.switchTo("/fr/opal/login-view.fxml");

        stage.setTitle("Opal");
        stage.setWidth(600);
        stage.setHeight(400);
        stage.setMinWidth(600);
        stage.setMinHeight(400);

        Image icon = new Image(getClass().getResourceAsStream("/fr/opal/opal-icon.png"));
        stage.getIcons().add(icon);

        stage.show();
    }
}
