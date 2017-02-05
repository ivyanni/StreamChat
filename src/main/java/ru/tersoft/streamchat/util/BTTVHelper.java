package ru.tersoft.streamchat.util;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Project streamchat.
 * Created by ivyanni on 31.01.2017.
 */
public class BTTVHelper {
    private static BTTVHelper bttvHelper = new BTTVHelper();
    private static final String EMOTE_API_URL = "https://api.betterttv.net/2/emotes";
    private static final String EMOTE_URL = "http://cdn.betterttv.net/emote/";
    private Map<String, String> emotes;

    private BTTVHelper() {

    }

    public static BTTVHelper getHelper() {
        return bttvHelper;
    }

    public void load() {
        emotes = new HashMap<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(EMOTE_API_URL)
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
                    if(obj.getInt("status") == 200) {
                        JSONArray emoteArray = obj.getJSONArray("emotes");
                        for(int i = 0; i < emoteArray.length(); i++) {
                            JSONObject emote = emoteArray.getJSONObject(i);
                            String code = emote.getString("code");
                            code = code.replaceAll("[(]", "[(]");
                            code = code.replaceAll("[)]", "[)]");
                            code = code.replaceAll("[*]", "[*]");
                            code = code.replaceAll("[:]", "[:]");
                            code = code.replaceAll("[']", "[']");
                            code = code.replaceAll("[?]", "[?]");
                            emotes.put(code,
                                    EMOTE_URL + emote.getString("id") + "/1x");
                        }
                    }
                }
            }
        });
    }

    public Map<String, String> getEmotes() {
        return emotes;
    }
}
