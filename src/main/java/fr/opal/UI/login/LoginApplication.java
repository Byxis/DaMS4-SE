package fr.opal.UI.login;

import fr.opal.service.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class LoginApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.initialize(stage);

        sceneManager.switchTo("/fr/opal/login-view.fxml");

        stage.setTitle("Opal");
        stage.show();
    }
}
