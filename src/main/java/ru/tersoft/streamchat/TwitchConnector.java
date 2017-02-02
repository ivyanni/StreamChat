package ru.tersoft.streamchat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Project streamchat.
 * Created by ivyanni on 21.01.2017.
 */
public class TwitchConnector {
    private static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    public final String AUTH_URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
                                        "?response_type=token" +
                                        "&client_id=" + CLIENT_ID +
                                        "&redirect_uri=http://localhost/twitch_oauth" +
                                        "&scope=chat_login+channel_read+user_read";
    private final String CHANNEL_URL = "https://api.twitch.tv/kraken/channel";
    private TwitchClient twitchClient;

    public TwitchConnector() {
    }

    public void startClient() {
        twitchClient = new TwitchClient("irc.chat.twitch.tv", 6667);
    }

    public String getTwitchUsername() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CHANNEL_URL+"?oauth_token="+DataStorage.getToken())
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            JSONObject obj = new JSONObject(response.body().string());
            DataStorage.setUsername(obj.getString("name"));
            startClient();
            return obj.getString("name");
        } else return null;
    }

    public void reloadClient() {
        twitchClient.stop();
        twitchClient = new TwitchClient("irc.chat.twitch.tv", 6667);
    }

    public void stopClient() {
        twitchClient.stop();
    }
}
