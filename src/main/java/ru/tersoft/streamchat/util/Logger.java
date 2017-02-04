package ru.tersoft.streamchat.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private double size;

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
        log.getEngine().loadContent(document.toString());
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
                Element editedLine = handleLine(content);
                if (editedLine != null) {
                    if (messages.size() > 20) {
                        Node first = log.getEngine().getDocument().getElementById("container").getFirstChild();
                        log.getEngine().getDocument().getElementById("container").removeChild(first);
                        messages.remove(0);
                    }
                    NodeList prevElements = log.getEngine().getDocument().getElementById("container").getElementsByTagName("div");
                    List<Element> nodes = new ArrayList<>();
                    for (int i = prevElements.getLength() - 1; i >= 0; i--) {
                        nodes.add((Element) prevElements.item(i));
                    }
                    Element element = log.getEngine().getDocument().createElement("div");
                    element.setAttribute("class", "well");
                    element.setAttribute("id", editedLine.getAttribute("id"));
                    element.appendChild(editedLine);
                    log.getEngine().getDocument().getElementById("container").appendChild(element);
                    String id = element.getAttribute("id");
                    Integer result = (Integer) log.getEngine().executeScript("document.getElementById('" + id + "').scrollHeight");
                    int height = result + 5;
                    for (Element node : nodes) {
                        id = node.getAttribute("id");
                        result = (Integer) log.getEngine().executeScript("document.getElementById('" + id + "').scrollHeight");
                        node.setAttribute("style", "bottom: " + height + "px;");
                        height += result + 5;
                    }
                    log.widthProperty().addListener(new SizeListener());
                }
            }
        });
    }

    class SizeListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            NodeList prevElements = log.getEngine().getDocument().getElementById("container").getElementsByTagName("div");
            List<Element> nodes = new ArrayList<>();
            for (int i = prevElements.getLength() - 1; i >= 0; i--) {
                nodes.add((Element) prevElements.item(i));
            }
            int height = 0;
            for (Element node : nodes) {
                String id = node.getAttribute("id");
                Integer result = (Integer) log.getEngine().executeScript("document.getElementById('" + id + "').scrollHeight");
                node.setAttribute("style", "bottom: " + height + "px;");
                height += result + 5;
            }
        }
    }

    private Element handleLine(String line) {
        DataStorage dataStorage = DataStorage.getDataStorage();
        if(line.startsWith(">")) {
            if(line.contains("PRIVMSG")) {
                ChatMessage message = new ChatMessage(Calendar.getInstance().getTime(), line);
                messages.add(message);
                return message.getElement(log.getEngine().getDocument());
            }
            else if(line.contains("CLEARCHAT")) {
                String username = line.substring(line.indexOf("CLEARCHAT #" + dataStorage.getUsername() + " :")
                        + 13 + dataStorage.getUsername().length());
                deleteMessages(username);
            }
            else if(line.contains("> :tmi.twitch.tv CAP * ACK :twitch.tv/tags")) {
                Element text = log.getEngine().getDocument().createElement("span");
                text.appendChild(log.getEngine().getDocument().createTextNode("Connected to "));
                Element channelName = log.getEngine().getDocument().createElement("strong");
                channelName.setTextContent("#" + dataStorage.getUsername());
                text.appendChild(channelName);
                text.setAttribute("id", "connected");
                return text;
            }
        } else {
            if(line.contains("< PASS")) {
                Node body = log.getEngine().getDocument().getElementsByTagName("body").item(0);
                Element container = log.getEngine().getDocument().createElement("div");
                container.setAttribute("id", "container");
                body.appendChild(container);
                Element text = log.getEngine().getDocument().createElement("span");
                text.appendChild(log.getEngine().getDocument().createTextNode("Connecting to "));
                Element channelName = log.getEngine().getDocument().createElement("strong");
                channelName.setTextContent("#" + dataStorage.getUsername());
                text.appendChild(channelName);
                text.appendChild(log.getEngine().getDocument().createTextNode("..."));
                text.setAttribute("id", "started");
                log.setVisible(true);
                return text;
            }
        }
        return null;
    }
}
