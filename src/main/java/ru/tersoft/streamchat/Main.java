package ru.tersoft.streamchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.tersoft.streamchat.controller.MainController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/layout/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Stream Chat");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
