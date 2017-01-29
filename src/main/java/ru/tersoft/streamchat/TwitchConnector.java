package ru.tersoft.streamchat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;

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

    private void connect() {
        twitchClient = new TwitchClient("irc.chat.twitch.tv", 6667);
        Timer timer = new Timer();
        timer.schedule(new ViewersUpdater(), 0, 5*60*1000);
    }

    public int start(String token) throws IOException {
        DataStorage.setToken(token);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CHANNEL_URL+"?oauth_token="+token)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            JSONObject obj = new JSONObject(response.body().string());
            DataStorage.setId(obj.getLong("_id"));
            DataStorage.setUsername(obj.getString("name"));
            connect();
        }
        return response.code();
    }

    public void reloadClient() {
        twitchClient.stop();
        twitchClient = new TwitchClient("irc.chat.twitch.tv", 6667);
        new ViewersUpdater().run();
    }
}
