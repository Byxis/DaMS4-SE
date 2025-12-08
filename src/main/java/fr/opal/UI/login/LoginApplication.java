package fr.opal.UI.login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("/fr/opal/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Opal");
        stage.setScene(scene);
        stage.show();
    }
}
