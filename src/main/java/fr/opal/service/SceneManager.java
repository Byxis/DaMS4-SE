package fr.opal.service;

import fr.opal.type.CachedScene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static SceneManager instance;

    private Stage primaryStage;
    private final Map<String, CachedScene> cache = new HashMap<>();

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

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
}