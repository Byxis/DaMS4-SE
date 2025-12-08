package fr.opal.controller;

import fr.opal.facade.LoginFacade;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    @FXML
    private void submitLogin() {
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();

        LoginFacade loginFacade = LoginFacade.getInstance();

        loginFacade.login(username, password);
    }
}
