module fr.opal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    opens fr.opal to javafx.fxml;
    opens fr.opal.UI to javafx.fxml;
    opens fr.opal.UI.login to javafx.fxml;
    opens fr.opal.controller to javafx.fxml;
    opens fr.opal.dao to javafx.fxml;
    opens fr.opal.facade to javafx.fxml;
    opens fr.opal.factory to javafx.fxml;
    opens fr.opal.service to javafx.fxml;
    opens fr.opal.manager to javafx.fxml;
    opens fr.opal.type to javafx.fxml;
    opens fr.opal.db to javafx.fxml;
    opens fr.opal.exception to javafx.fxml;

    exports fr.opal.UI;
    exports fr.opal.UI.login;
    exports fr.opal.controller;
    exports fr.opal.dao;
    exports fr.opal.facade;
    exports fr.opal.factory;
    exports fr.opal.service;
    exports fr.opal.manager;
    exports fr.opal.type;
    exports fr.opal.db;
    exports fr.opal.exception;
}