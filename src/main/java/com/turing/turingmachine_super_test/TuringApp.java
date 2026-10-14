package com.turing.turingmachine_super_test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TuringApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/turing/turingmachine_super_test/turing.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Эмулятор Машины Тьюринга");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
