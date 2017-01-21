package ru.tersoft.streamchat;

/**
 * Project streamchat.
 * Created by ivyanni on 21.01.2017.
 */
public class TwitchApi {
    private static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    private final String SERVER = "";
    private final String PORT = "";
    public static final String AUTH_URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
                                        "?response_type=token" +
                                        "&client_id=" + CLIENT_ID +
                                        "&redirect_uri=http://localhost/twitch_oauth" +
                                        "&scope=chat_login+channel_read";
    private String token;

    public TwitchApi(String token) {
        this.token = token;
    }
}
