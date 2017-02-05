package ru.tersoft.streamchat.util;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Project streamchat.
 * Created by ivyanni on 05.02.2017.
 */
public class BadgeLoader {
    private static BadgeLoader badgeLoader = new BadgeLoader();
    private static final String CHAT_URL = "https://badges.twitch.tv/v1/badges/global/display?language=en";
    private Map<String, String> badges;

    private BadgeLoader() {
    }

    public static BadgeLoader getLoader() {
        return badgeLoader;
    }

    public void load() {
        badges = new HashMap<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CHAT_URL)
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
                    JSONObject sets = obj.getJSONObject("badge_sets");
                    Iterator i = sets.keys();
                    while (i.hasNext()) {
                        String name = (String) i.next();
                        JSONObject badgeName = sets.getJSONObject(name);
                        JSONObject badgeVersions = badgeName.getJSONObject("versions");
                        Iterator j = badgeVersions.keys();
                        while(j.hasNext()) {
                            String version = (String) j.next();
                            JSONObject badgeVersion = badgeVersions.getJSONObject(version);
                            String url = badgeVersion.getString("image_url_1x");
                            badges.put(name+"/"+version, url);
                        }
                    }
                }
            }
        });
    }

    public Map<String, String> getBadges() {
        return badges;
    }
}
