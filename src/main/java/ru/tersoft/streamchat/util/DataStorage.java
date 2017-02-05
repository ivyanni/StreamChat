package ru.tersoft.streamchat.util;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class DataStorage {
    private static DataStorage dataStorage = new DataStorage();
    public static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    private String token = null;
    private String username = null;
    private String subBadge = null;

    private DataStorage() {
    }

    public static DataStorage getDataStorage() {
        return dataStorage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSubBadge() {
        return subBadge;
    }

    public void setSubBadge(String subBadge) {
        this.subBadge = subBadge;
    }
}
