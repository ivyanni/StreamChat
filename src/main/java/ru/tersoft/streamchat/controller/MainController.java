package ru.tersoft.streamchat.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import ru.tersoft.streamchat.BTTVHelper;
import ru.tersoft.streamchat.DataStorage;
import ru.tersoft.streamchat.Logger;
import ru.tersoft.streamchat.TwitchConnector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.prefs.Preferences;

/**
 * Project streamchat.
 * Created by ivyanni on 31.01.2017.
 */
public class MainController {
    private final String PREF_TOKEN = "access_token";
    private final String PREF_NAME = "username";
    private TwitchConnector twitchConnector;
    private Preferences prefs;
    private WebView log;

    public MainController(WebView web) {
        log = web;
        Logger.getLogger().setLogArea(log);
        BTTVHelper.getHelper().load();
        log.getEngine().documentProperty().addListener(new DocListener());
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

    class DocListener implements ChangeListener<Document> {
        @Override
        public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
            try {
                Field f = log.getEngine().getClass().getDeclaredField("page");
                f.setAccessible(true);
                com.sun.webkit.WebPage page = (com.sun.webkit.WebPage) f.get(log.getEngine());
                page.setBackgroundColor((new java.awt.Color(0, 0, 0, 0)).getRGB());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
       // popup.initOwner(primaryStage);

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

    public void stopAllClients() {
        twitchConnector.stopClient();
    }
}
