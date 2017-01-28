package ru.tersoft.streamchat;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class DataStorage {
    private static String token;
    private static String channel;
    private static String username;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        DataStorage.token = token;
    }

    public static String getChannel() {
        return channel;
    }

    public static void setChannel(String channel) {
        DataStorage.channel = channel;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        DataStorage.username = username;
    }
}
