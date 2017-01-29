package ru.tersoft.streamchat.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import ru.tersoft.streamchat.DataStorage;
import ru.tersoft.streamchat.Logger;
import ru.tersoft.streamchat.TwitchConnector;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainController implements Initializable {
    private final String PREF_TOKEN = "access_token";
    private Stage primaryStage;
    private TwitchConnector twitchConnector;
    private Preferences prefs;

    @FXML
    private TextArea log;
    @FXML
    private Label username;
    @FXML
    private Label viewers;
    @FXML
    private StatusBar statusBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        username.textProperty().bind(DataStorage.getUsernameProperty());
        viewers.textProperty().bind(DataStorage.getViewersProperty());
        statusBar.textProperty().bind(DataStorage.getActiveStatusProperty());
        twitchConnector = new TwitchConnector();
        prefs = Preferences.userNodeForPackage(TwitchConnector.class);
        String token = prefs.get(PREF_TOKEN, null);
        if(token != null) {
            startTwitchChat(token);
        } else {
            authorize();
        }
    }

    @FXML
    public void reload() {
        log.clear();
        twitchConnector.reloadClient();
    }

    private void startTwitchChat(String token) {
        try {
            int responseCode = twitchConnector.start(token);
            if (responseCode != 200) {
                authorize();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void authorize() {
        WebView webView = new WebView();
        webView.getEngine().load(twitchConnector.AUTH_URL);

        Stage popup = new Stage();
        popup.setScene(new Scene((webView)));
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setIconified(false);
        popup.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        popup.setWidth(370);
        popup.setHeight(445);
        popup.setResizable(false);
        popup.initOwner(primaryStage);

        webView.getEngine().locationProperty().addListener((observableValue, ov, url) -> {
            if (url.startsWith("http://localhost")) {
                String newToken = url.substring(url.indexOf("=") + 1, url.indexOf("&"));
                prefs.put(PREF_TOKEN, newToken);
                startTwitchChat(newToken);
                popup.close();
            }
        });
        popup.showAndWait();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            twitchConnector.stopClient();
            Platform.exit();
            System.exit(0);
        });
        Logger.setLogArea(log);
    }
}
