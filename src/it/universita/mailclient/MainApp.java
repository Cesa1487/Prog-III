package it.universita.mailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/it/universita/mailclient/view/login_view.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Mail Client - Login");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
