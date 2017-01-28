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
public class TwitchApi {
    private static final String CLIENT_ID = "y28js5lzpxxlsmasu42tnal77l1oox";
    public static final String AUTH_URL = "https://api.twitch.tv/kraken/oauth2/authorize" +
                                        "?response_type=token" +
                                        "&client_id=" + CLIENT_ID +
                                        "&redirect_uri=http://localhost/twitch_oauth" +
                                        "&scope=chat_login+channel_read+user_read";
    private Client client;
    private final String USERINFO_URL = "https://api.twitch.tv/kraken/channel";

    public TwitchApi() {
    }

    private void connect() {
        client = new Client("irc.chat.twitch.tv", 6667);
    }

    public void sendMessage(String str){
        client.sendCommand("PRIVMSG #" + DataStorage.getUsername() + " :" + str);
    }

    public int start(String token, String channel) throws IOException {
        DataStorage.setToken(token);
        DataStorage.setChannel(channel);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(USERINFO_URL+"?oauth_token="+token)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            JSONObject obj = new JSONObject(response.body().string());
            DataStorage.setUsername(obj.getString("name"));
            connect();
        }
        return response.code();
    }
}
