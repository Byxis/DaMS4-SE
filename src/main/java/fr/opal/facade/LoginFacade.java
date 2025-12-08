package fr.opal.facade;

import fr.opal.service.UserManager;

public class LoginFacade {
    private static LoginFacade instance;

    public static LoginFacade getInstance() {
        if (instance == null) {
            instance = new LoginFacade();
        }
        return instance;
    }

    private LoginFacade() {
    }

    public void login(String username, String password) {
        UserManager.getInstance().login(username, password);
    }
}
