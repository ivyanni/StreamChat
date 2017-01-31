package ru.tersoft.streamchat;

import com.sun.deploy.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project streamchat.
 * Created by ivyanni on 30.01.2017.
 */
public class ChatMessage {
    private Date time;
    private String[] badges;
    private String[] emotes;
    private String nameColor;
    private String username;
    private String displayName;
    private String message;

    public ChatMessage(Date time, String line) {
        // Set time
        this.time = time;
        // Set empty displayName
        displayName = "";
        // Set name color
        int startIndex = line.indexOf("color=") + 6;
        nameColor = line.substring(startIndex, line.indexOf(";", startIndex));
        // Set badges
        startIndex = line.indexOf("@badges=") + 8;
        String badgesLine = line.substring(startIndex, line.indexOf(";"));
        if(badgesLine.length() > 0) {
            badges = badgesLine.split(",");
            for (String badge : badges) {
                String badgeName = badge.substring(0, badge.indexOf("/"));
                if(badgeName.equals("moderator")) badgeName = "mod";
                displayName += "<img style=\"vertical-align:middle\" src=\"https://static-cdn.jtvnw.net/chat-badges/" + badgeName + ".png\" />";
            }
        }
        // Set display name
        startIndex = line.indexOf("display-name=") + 13;
        displayName += " " + line.substring(startIndex, line.indexOf(";", startIndex));
        // Set message
        startIndex = line.indexOf("PRIVMSG #"+DataStorage.getUsername()+" :") + 11 + DataStorage.getUsername().length();
        message = line.substring(startIndex);
        // Set username
        Pattern pattern = Pattern.compile(".*@(.*?).tmi.twitch.tv");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            username = matcher.group(1);
        }
        // Set emotes
        startIndex = line.indexOf("emotes=") + 7;
        String emotesLine = line.substring(startIndex, line.indexOf(";", startIndex));
        if(emotesLine.length() > 0) {
            emotes = emotesLine.split("/");
            Map<String,String> tokens = new HashMap<>();
            for (String emote : emotes) {
                int index = emote.indexOf(":");
                String emoteId = emote.substring(0, index);
                String[] emotesPos = emote.substring(index+1).split(",");
                for(String pos : emotesPos) {
                    int firstIndex = Integer.parseInt(pos.substring(0, pos.indexOf("-")));
                    int lastIndex = Integer.parseInt(pos.substring(pos.indexOf("-") + 1));
                    if(firstIndex < message.length() && lastIndex+1 < message.length()) {
                        tokens.put(message.substring(firstIndex, lastIndex + 1),
                                "<img style=\"vertical-align:middle\" src=\"https://static-cdn.jtvnw.net/emoticons/v1/" + emoteId + "/1.0\" />");
                    } else break;
                }
            }
            String patternString = "(" + StringUtils.join(tokens.keySet(), "|") + ")";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(message);

            StringBuffer sb = new StringBuffer();
            while(matcher.find()) {
                matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
            }
            matcher.appendTail(sb);

            message = sb.toString();
        }
    }

    public String getUsername() {
        return username;
    }

    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU"));
        String timeTag = "<font color=grey size=1>[" + simpleDateFormat.format(time) + "]</font> ";
        return timeTag + "<font color=\"" + nameColor + "\">" + displayName + "</font>: " + message;
    }
}
