package fr.opal.type;

import javafx.scene.Scene;

/**
 * A cached scene with its controller
 */
public class CachedScene {
    private Scene scene;
    private Object controller;

    public CachedScene(Scene scene, Object controller) {
        this.scene = scene;
        this.controller = controller;
    }

    /**
     * Gets the scene.
     * @return The scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Gets the controller.
     * @return The controller
     */
    public Object getController() {
        return controller;
    }
}
