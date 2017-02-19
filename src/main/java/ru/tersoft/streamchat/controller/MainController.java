package ru.tersoft.streamchat.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.tersoft.streamchat.MainFrame;
import ru.tersoft.streamchat.TwitchConnector;
import ru.tersoft.streamchat.util.BTTVHelper;
import ru.tersoft.streamchat.util.BadgeLoader;
import ru.tersoft.streamchat.util.DataStorage;
import ru.tersoft.streamchat.util.Logger;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Project streamchat.
 * Created by ivyanni on 31.01.2017.
 */
public class MainController {
    private static final String AUTH_URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
            "?response_type=token" +
            "&client_id=" + DataStorage.CLIENT_ID +
            "&redirect_uri=http://localhost/twitch_oauth" +
            "&scope=chat_login+channel_read";
    private static final String PREF_TOKEN = "access_token";
    private static final String PREF_NAME = "username";
    private TwitchConnector twitchConnector;
    private Preferences prefs;
    private ResourceBundle bundle;

    public MainController(WebView web, ResourceBundle bundle) {
        Logger.getLogger().start(web, bundle);
        this.bundle = bundle;
        prefs = Preferences.userNodeForPackage(MainFrame.class);
        BadgeLoader.getLoader().load();
        if(prefs.getBoolean("bttv", true)) {
            BTTVHelper.getHelper().load();
        } else DataStorage.getDataStorage().setBttvEnabled(false);
        final com.sun.webkit.WebPage webPage = com.sun.javafx.webkit.Accessor.getPageFor(web.getEngine());
        webPage.setBackgroundColor(0);
        twitchConnector = new TwitchConnector();
        String token = prefs.get(PREF_TOKEN, null);
        String username = prefs.get(PREF_NAME, null);
        if(token != null) {
            startTwitchChat(token, username);
        } else {
            authorize();
        }
    }

    public void reload() {
        Logger.getLogger().reload();
        twitchConnector.reloadClient();
    }

    private void startTwitchChat(String token, String username) {
        try {
            DataStorage.getDataStorage().setToken(token);
            if(username == null) {
                username = twitchConnector.getTwitchUsername();
                if (username == null) {
                    authorize();
                } else {
                    prefs.put(PREF_NAME, username);
                }
            } else {
                DataStorage.getDataStorage().setUsername(username);
                twitchConnector.receiveSubBadge();
                twitchConnector.startClient();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void authorize() {
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.getEngine().load(AUTH_URL);

            Stage popup = new Stage();
            popup.setScene(new Scene((webView)));
            popup.initStyle(StageStyle.UTILITY);
            popup.setTitle(bundle.getString("required_auth"));
            popup.setWidth(370);
            popup.setHeight(445);
            popup.setAlwaysOnTop(true);
            popup.setResizable(false);
            popup.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            webView.getEngine().locationProperty().addListener((observableValue, ov, url) -> {
                if (url.startsWith("http://localhost")) {
                    String newToken = url.substring(url.indexOf("=") + 1, url.indexOf("&"));
                    prefs.put(PREF_TOKEN, newToken);
                    startTwitchChat(newToken, null);
                    popup.close();
                }
            });
            popup.show();
        });
    }

    public void stopAllClients() {
        twitchConnector.stopClient();
    }
}
