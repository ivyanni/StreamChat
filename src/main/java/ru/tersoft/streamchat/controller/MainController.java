package ru.tersoft.streamchat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.tersoft.streamchat.Logger;
import ru.tersoft.streamchat.TwitchApi;

import java.io.IOException;
import java.util.prefs.Preferences;

public class MainController {
    private final String PREF_TOKEN = "access_token";
    private Stage primaryStage;
    private TwitchApi twitchApi;
    private Preferences prefs;

    @FXML
    private TextArea log;

    @FXML
    private TextField message;

    @FXML
    private void handleConnect() {
        twitchApi = new TwitchApi();
        prefs = Preferences.userNodeForPackage(ru.tersoft.streamchat.TwitchApi.class);
        String token = prefs.get(PREF_TOKEN, null);
        if(token != null) {
            Logger.printLine("Take access token from prefs: " + token);
            startTwitchChat(token);
        } else {
            authorize();
        }
    }

    private void startTwitchChat(String token) {
        try {
            int responseCode = twitchApi.start(token);
            if (responseCode != 200) {
                authorize();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void authorize() {
        WebView webView = new WebView();
        webView.getEngine().load(TwitchApi.AUTH_URL);

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
                Logger.printLine("Save new access token: " + newToken);
                prefs.put(PREF_TOKEN, newToken);
                startTwitchChat(newToken);
                popup.close();
            }
        });
        popup.showAndWait();
    }

    @FXML
    private void handleEnterPressed(ActionEvent ae){
        handleSend();
    }

    @FXML
    private void handleSend() {
        twitchApi.sendMessage(message.getText());
        message.clear();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        Logger.setLogArea(log);
    }
}
