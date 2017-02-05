package ru.tersoft.streamchat.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class DataStorage {
    private static DataStorage dataStorage = new DataStorage();
    public static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    private StringProperty token = new SimpleStringProperty();
    private StringProperty username = new SimpleStringProperty();

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
}
