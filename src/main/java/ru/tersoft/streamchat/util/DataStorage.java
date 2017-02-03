package ru.tersoft.streamchat.util;

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
    private static DataStorage dataStorage = new DataStorage();
    public static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    private StringProperty viewers = new SimpleStringProperty("0");
    private StringProperty token = new SimpleStringProperty();
    private StringProperty username = new SimpleStringProperty();
    private ObjectProperty<Image> activeStatus = new SimpleObjectProperty<>();

    private DataStorage() {
    }

    public static DataStorage getDataStorage() {
        return dataStorage;
    }

    public String getToken() {
        return token.getValue();
    }

    public void setToken(String token) {
        this.token.setValue(token);
    }

    public String getUsername() {
        return username.getValue();
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public StringProperty getViewersProperty() {
        return viewers;
    }

    void setViewers(Long viewers) {
        Platform.runLater(() -> this.viewers.setValue(viewers.toString()));
    }

    public ObjectProperty<Image> getActiveStatusProperty() {
        return activeStatus;
    }

    public void setActiveStatus(Boolean activeStatus) {
    }
}
