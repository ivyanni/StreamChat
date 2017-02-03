package ru.tersoft.streamchat.util;

import javafx.application.Platform;
import javafx.scene.web.WebView;
import ru.tersoft.streamchat.entity.ChatMessage;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

/**
 * Project streamchat.
 * Created by ivyanni on 22.01.2017.
 */
public class Logger {
    private static Logger logger = new Logger();
    private WebView log;
    private List<ChatMessage> messages;
    private StringBuilder header;
    private StringBuilder document;

    private Logger() {

    }

    public static Logger getLogger() {
        return logger;
    }

    public void setLogArea(WebView textArea) {
        log = textArea;
        log.setContextMenuEnabled(false);
        log.getEngine().setJavaScriptEnabled(true);
        document = new StringBuilder();
        header = new StringBuilder();
        try {
            URL url = getClass().getResource("/header.html");
            Stream<String> strings = Files.lines(Paths.get(url.toURI()), Charset.defaultCharset());
            strings.forEach(s -> header.append(s));
        } catch(Exception e) {
            e.printStackTrace();
        }
        document.append(header);
        messages = new ArrayList<>();
    }

    private void deleteMessages(String username) {
        messages.removeIf(message -> username.contains(message.getUsername()));
        document = new StringBuilder().append(header.toString());
        for(ChatMessage message : messages) {
            document.append(message.toString());
        }
        log.getEngine().loadContent(document.toString());
    }

    public void printLine(String content) {
        Platform.runLater(() -> {
            if(log != null) {
                String editedLine = handleLine(content);
                if(editedLine != null) {
                    document.append(editedLine);
                    log.getEngine().loadContent(document.toString());
                }
            }
        });
    }

    private String handleLine(String line) {
        DataStorage dataStorage = DataStorage.getDataStorage();
        if(line.startsWith(">")) {
            if(line.contains("PRIVMSG")) {
                ChatMessage message = new ChatMessage(Calendar.getInstance().getTime(), line);
                messages.add(message);
                return message.toString();
            }
            else if(line.contains("CLEARCHAT")) {
                String username = line.substring(line.indexOf("CLEARCHAT #" + dataStorage.getUsername() + " :")
                        + 13 + dataStorage.getUsername().length());
                deleteMessages(username);
            }
            else if(line.contains("> :tmi.twitch.tv CAP * ACK :twitch.tv/tags")) {
                String str = "<div class=\"well\"> Connected to <strong>#" + dataStorage.getUsername() + "</strong>.</div>";
                header.append(str);
                document.append(str);
                log.getEngine().loadContent(document.toString());
                log.setVisible(true);
            }
        } else {
            if(line.contains("< PASS")) {
                String str = "<div class=\"well\"> Connecting to <strong>#" + dataStorage.getUsername() + "</strong>...</div>";
                header.append(str);
                document.append(str);
                log.getEngine().loadContent(document.toString());
            }
        }
        return null;
    }
}
