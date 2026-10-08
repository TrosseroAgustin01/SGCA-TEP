package com.sgcatep;

import com.sgcatep.view.LoginFX;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        LoginFX login = new LoginFX();
        login.show(primaryStage);
    }

    @Override
    public void stop() {
        ConexionDB.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}