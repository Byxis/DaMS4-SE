package fr.opal.type;

import javafx.scene.Scene;

public class CachedScene {
    private Scene scene;
    private Object controller;

    public CachedScene(Scene scene, Object controller) {
        this.scene = scene;
        this.controller = controller;
    }

    public Scene getScene() {
        return scene;
    }

    public Object getController() {
        return controller;
    }
}
