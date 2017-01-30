package ru.tersoft.streamchat;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.TimerTask;

/**
 * Project streamchat.
 * Created by ivyanni on 29.01.2017.
 */
public class ViewersUpdater extends TimerTask {
    private final String STREAM_URL = "https://api.twitch.tv/kraken/streams/";

    public void run() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(STREAM_URL+DataStorage.getUsername()+"?oauth_token="+DataStorage.getToken())
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    JSONObject obj = new JSONObject(response.body().string());
                    if(!obj.isNull("stream")) {
                        JSONObject stream = obj.getJSONObject("stream");
                        DataStorage.setViewers(stream.getLong("viewers"));
                    } else DataStorage.setViewers(0L);
                }
            }
        });
    }
}
