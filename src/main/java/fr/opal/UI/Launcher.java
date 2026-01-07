package fr.opal.UI;

import javafx.application.Application;
import fr.opal.UI.login.LoginApplication;
import javafx.scene.text.Font;

/**
 * Launcher class to start the JavaFX application.
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(LoginApplication.class, args);
    }
}
