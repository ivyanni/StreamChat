package ru.tersoft.streamchat;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class DataStorage {
    private static Long id;
    private static StringProperty viewers = new SimpleStringProperty("0");
    private static StringProperty token = new SimpleStringProperty();
    private static StringProperty username = new SimpleStringProperty();

    public static String getToken() {
        return token.getValue();
    }

    public static void setToken(String token) {
        DataStorage.token.setValue(token);
    }

    public static Long getId() {
        return id;
    }

    public static void setId(Long id) {
        DataStorage.id = id;
    }

    public static StringProperty getUsernameProperty() {
        return username;
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

    public static Long getViewers() {
        return Long.parseLong(viewers.getValue());
    }

    public static void setViewers(Long viewers) {
        DataStorage.viewers.setValue(viewers.toString());
    }
}
