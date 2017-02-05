package ru.tersoft.streamchat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import ru.tersoft.streamchat.client.TwitchClient;
import ru.tersoft.streamchat.util.DataStorage;

import java.io.IOException;

/**
 * Project streamchat.
 * Created by ivyanni on 21.01.2017.
 */
public class TwitchConnector {
    private static final String CHANNEL_URL = "https://api.twitch.tv/kraken/channel";
    private TwitchClient twitchClient;

    public TwitchConnector() {
    }

    public void startClient() {
        twitchClient = new TwitchClient();
        twitchClient.start();
    }

    public String getTwitchUsername() throws IOException {
        DataStorage dataStorage = DataStorage.getDataStorage();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CHANNEL_URL + "?oauth_token=" + dataStorage.getToken())
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            JSONObject obj = new JSONObject(response.body().string());
            dataStorage.setUsername(obj.getString("name"));
            startClient();
            return obj.getString("name");
        } else return null;
    }

    public void reloadClient() {
        if(twitchClient != null) {
            twitchClient.stop();
            twitchClient = new TwitchClient();
            twitchClient.start();
        }
    }

    public void stopClient() {
        if(twitchClient != null) {
            twitchClient.stop();
        }
    }
}
