package ru.tersoft.streamchat;

import javafx.application.Platform;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    public static Logger getLogger() {
        return logger;
    }

    public void setLogArea(WebView textArea) {
        log = textArea;
        log.setContextMenuEnabled(false);
        document = new StringBuilder();
        header = new StringBuilder().append("<html>");
        header.append("<head>");
        header.append("<style> body { background-color:black; color:white; } </style>");
        header.append("   <script language=\"javascript\" type=\"text/javascript\">");
        header.append("       function toBottom(){");
        header.append("           window.scrollTo(0, document.body.scrollHeight);");
        header.append("       }");
        header.append("   </script>");
        header.append("</head>");
        header.append("<body style='font-family:Verdana; font-size: 13' onload='toBottom()'>");
        document.append(header);
        messages = new ArrayList<>();
    }

    private void deleteMessages(String username) {
        messages.removeIf(message -> username.contains(message.getUsername()));
        document = new StringBuilder().append(header.toString());
        for(ChatMessage message : messages) {
            document.append(message.toString()).append("<br />");
        }
        log.getEngine().loadContent(document.toString());
    }

    public void printLine(String content) {
        Platform.runLater(() -> {
            if(log != null) {
                String editedLine = handleLine(content);
                if(editedLine != null) {
                    document.append(editedLine).append("<br />");
                    log.getEngine().loadContent(document.toString());
                }
            }
        });
    }

    private String handleLine(String line) {
        if(line.startsWith(">")) {
            if(line.contains("PRIVMSG")) {
                ChatMessage message = new ChatMessage(Calendar.getInstance().getTime(), line);
                messages.add(message);
                return message.toString();
            }
            else if(line.contains("CLEARCHAT")) {
                String username = line.substring(line.indexOf("CLEARCHAT #" + DataStorage.getUsername() + " :")
                        + 13 + DataStorage.getUsername().length());
                deleteMessages(username);
            }
            else if(line.contains("> :tmi.twitch.tv CAP * ACK :twitch.tv/tags")) {
                String str = "Connected to #" + DataStorage.getUsername() + ".<br />";
                header.append(str);
                document.append(str);
                log.getEngine().loadContent(document.toString());
            }
        } else {
            if(line.contains("< PASS")) {
                String str = "Connecting to #" + DataStorage.getUsername() + "...<br />";
                header.append(str);
                document.append(str);
                log.getEngine().loadContent(document.toString());
            }
        }
        return null;
    }
}
