package ru.tersoft.streamchat.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.tersoft.streamchat.TwitchApi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainController {
    private Stage primaryStage;

    @FXML
    private TextArea log;

    @FXML
    private void handleConnect() {
        WebView webView = new WebView();
        webView.getEngine().load(TwitchApi.AUTH_URL);

        Stage popup = new Stage();
        popup.setScene(new Scene((webView)));
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Authorizing...");
        popup.setIconified(false);
        popup.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        popup.setWidth(370);
        popup.setHeight(455);
        popup.initOwner(primaryStage);

        StringBuilder result = new StringBuilder();
        webView.getEngine().locationProperty().addListener((observableValue, ov, url) -> {
            if (url.startsWith("http://localhost")) {
                String token = url.substring(url.indexOf("=") + 1, url.indexOf("&"));
                result.append(token);
                log.setText(getTimeTag() + "Got access token: " + result.toString());
                popup.close();
            }
        });
        popup.showAndWait();
    }

    private String getTimeTag() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"));
        return "[" + simpleDateFormat.format(date) + "] ";
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
