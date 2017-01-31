package ru.tersoft.streamchat.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.tersoft.streamchat.DataStorage;
import ru.tersoft.streamchat.Logger;
import ru.tersoft.streamchat.TwitchConnector;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainController implements Initializable {
    private final String PREF_TOKEN = "access_token";
    private final String PREF_NAME = "username";
    private Stage primaryStage;
    private TwitchConnector twitchConnector;
    private Preferences prefs;

    @FXML
    private WebView log;
    @FXML
    private Label viewers;
    @FXML
    private ImageView status;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewers.textProperty().bind(DataStorage.getViewersProperty());
        status.imageProperty().bind(DataStorage.getActiveStatusProperty());
        twitchConnector = new TwitchConnector();
        prefs = Preferences.userNodeForPackage(TwitchConnector.class);
        String token = prefs.get(PREF_TOKEN, null);
        String username = prefs.get(PREF_NAME, null);
        if(token != null) {
            startTwitchChat(token, username);
        } else {
            authorize();
        }
    }

    @FXML
    public void reload() {
        Logger.getLogger().setLogArea(log);
        twitchConnector.reloadClient();
    }

    private void startTwitchChat(String token, String username) {
        try {
            DataStorage.setToken(token);
            if(username == null) {
                username = twitchConnector.getTwitchUsername();
                if (username == null) {
                    authorize();
                } else {
                    prefs.put(PREF_NAME, username);
                }
            } else {
                DataStorage.setUsername(username);
                twitchConnector.startClient();
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
                startTwitchChat(newToken, null);
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
        Logger.getLogger().setLogArea(log);
    }
}
