package fr.opal.service;

import fr.opal.type.CachedScene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
}