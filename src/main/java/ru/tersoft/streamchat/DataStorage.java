package ru.tersoft.streamchat;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class DataStorage {
    private static StringProperty viewers = new SimpleStringProperty("0");
    private static StringProperty token = new SimpleStringProperty();
    private static StringProperty username = new SimpleStringProperty();
    private static ObjectProperty<Image> activeStatus = new SimpleObjectProperty<>(new Image("/layout/img/offline.png"));

    public static String getToken() {
        return token.getValue();
    }

    public static void setToken(String token) {
        DataStorage.token.setValue(token);
    }

    public static String getUsername() {
        return username.getValue();
    }

    public static void setUsername(String username) {
        DataStorage.username.setValue(username);
    }

    public static StringProperty getViewersProperty() {
        return viewers;
    }

    public static void setViewers(Long viewers) {
        Platform.runLater(() -> DataStorage.viewers.setValue(viewers.toString()));
    }

    public static ObjectProperty<Image> getActiveStatusProperty() {
        return activeStatus;
    }

    public static void setActiveStatus(Boolean activeStatus) {
        Platform.runLater(() -> {
            if(activeStatus)
                DataStorage.activeStatus.setValue(new Image("/layout/img/online.png"));
            else DataStorage.activeStatus.setValue(new Image("/layout/img/offline.png"));
        });
    }
}
