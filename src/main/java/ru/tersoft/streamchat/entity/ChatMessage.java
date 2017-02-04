package ru.tersoft.streamchat.entity;

import com.sun.deploy.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ru.tersoft.streamchat.util.BTTVHelper;
import ru.tersoft.streamchat.util.DataStorage;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project streamchat.
 * Created by ivyanni on 30.01.2017.
 */
public class ChatMessage {
    private Date time;
    private String id;
    private List<String> badges;
    Map<String,String> tokens;
    private String nameColor;
    private String username;
    private String displayName;
    private String message;

    public ChatMessage(Date time, String line) {
        DataStorage dataStorage = DataStorage.getDataStorage();
        // Set time
        this.time = time;
        // Set empty displayName
        displayName = "";
        // Set name color
        int startIndex = line.indexOf("color=") + 6;
        nameColor = line.substring(startIndex, line.indexOf(";", startIndex));
        // Set id
        startIndex = line.indexOf("id=") + 3;
        id = line.substring(startIndex, line.indexOf(";", startIndex));
        // Set badges
        handleBadges(line);
        // Set display name
        startIndex = line.indexOf("display-name=") + 13;
        displayName += " " + line.substring(startIndex, line.indexOf(";", startIndex));
        // Set message
        startIndex = line.indexOf("PRIVMSG #" + dataStorage.getUsername() + " :") + 11 + dataStorage.getUsername().length();
        message = line.substring(startIndex);
        // Set username
        Pattern pattern = Pattern.compile(".*@(.*?).tmi.twitch.tv");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            username = matcher.group(1);
        }
        // Set emotes
        handleEmotes(line);
    }

    private void handleBadges(String line) {
        int startIndex = line.indexOf("@badges=") + 8;
        String badgesLine = line.substring(startIndex, line.indexOf(";"));
        if(badgesLine.length() > 0) {
            String[] allBadges = badgesLine.split(",");
            badges = new ArrayList<>();
            for (String badge : allBadges) {
                String badgeName = badge.substring(0, badge.indexOf("/"));
                if(badgeName.equals("moderator")) badgeName = "mod";
                badges.add("https://static-cdn.jtvnw.net/chat-badges/" + badgeName + ".png");
            }
        }
    }

    private void handleEmotes(String line) {
        int startIndex = line.indexOf("emotes=") + 7;
        tokens = new HashMap<>();
        String emotesLine = line.substring(startIndex, line.indexOf(";", startIndex));
        if(emotesLine.length() > 0) {
            String[] emotes = emotesLine.split("/");
            for (String emote : emotes) {
                int index = emote.indexOf(":");
                String emoteId = emote.substring(0, index);
                String[] emotesPos = emote.substring(index+1).split(",");
                for(String pos : emotesPos) {
                    int firstIndex = Integer.parseInt(pos.substring(0, pos.indexOf("-")));
                    int lastIndex = Integer.parseInt(pos.substring(pos.indexOf("-") + 1));
                    if(firstIndex <= message.length() && lastIndex+1 <= message.length()) {
                        tokens.put(message.substring(firstIndex, lastIndex+1),
                                "https://static-cdn.jtvnw.net/emoticons/v1/" + emoteId + "/1.0");
                    } else break;
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public Element getElement(Document document) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU"));

        Element nameNode = document.createElement("font");
        nameNode.setAttribute("color", nameColor);
        for(String badge : badges) {
            Element img = document.createElement("img");
            img.setAttribute("src", badge);
            nameNode.appendChild(img);
        }
        Text name = document.createTextNode(displayName + ": ");
        nameNode.appendChild(name);

        Element messageNode = document.createElement("span");

        Map<String, String> emotes = new HashMap<>();
        emotes.putAll(tokens);
        // Set BTTV emotes
        emotes.putAll(BTTVHelper.getHelper().getEmotes());
        String patternString = "^(" + StringUtils.join(emotes.keySet(), "|") + ")$";
        Pattern pattern = Pattern.compile(patternString);
        String[] words = message.split(" ");
        for(String word : words) {
            if(word.startsWith("@")) {
                Element text = document.createElement("strong");
                text.setTextContent(word);
                messageNode.appendChild(text);
                continue;
            }
            Matcher matcher = pattern.matcher(word);
            if(matcher.find()) {
                String str = matcher.group(1);
                str = str.replaceAll("[(]", "[(]");
                str = str.replaceAll("[)]", "[)]");
                str = str.replaceAll("[*]", "[*]");
                str = str.replaceAll("[:]", "[:]");
                str = str.replaceAll("[']", "[']");
                Element img = document.createElement("img");
                img.setAttribute("src", emotes.get(str));
                img.setAttribute("class", "emote");
                messageNode.appendChild(img);
            } else {
                Text msg = document.createTextNode(word);
                messageNode.appendChild(msg);
            }
        }

        Element div = document.createElement("span");
        Element timeNode = document.createElement("font");
        timeNode.setAttribute("color", "grey");
        timeNode.setAttribute("size", "1");
        timeNode.setTextContent("[" + simpleDateFormat.format(time) + "] ");
        Element strongNode = document.createElement("strong");
        strongNode.appendChild(nameNode);
        div.appendChild(timeNode);
        div.appendChild(strongNode);
        div.appendChild(messageNode);
        div.setAttribute("id", id);
        return div;
    }
}
