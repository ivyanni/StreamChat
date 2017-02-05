package ru.tersoft.streamchat.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.tersoft.streamchat.entity.ChatMessage;

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

    private Logger() {

    }

    public static Logger getLogger() {
        return logger;
    }

    public void setLogArea(WebView textArea) {
        log = textArea;
        log.setContextMenuEnabled(false);
        log.getEngine().setJavaScriptEnabled(true);
        log.getEngine().loadContent("<html>\n");
        log.widthProperty().addListener(new SizeListener());
        messages = new ArrayList<>();
    }

    private void deleteMessages(String username) {
        Element container = log.getEngine().getDocument().getElementById("container");
        for(ChatMessage message : messages) {
            if(message.getUsername().equals(username)) {
                Element msg = log.getEngine().getDocument().getElementById(message.getId());
                container.removeChild(msg);
            }
        }
        messages.removeIf(message -> username.contains(message.getUsername()));
        NodeList elements = container.getElementsByTagName("div");
        List<Element> nodes = new ArrayList<>();
        for (int i = elements.getLength() - 1; i >= 0; i--) {
            nodes.add((Element) elements.item(i));
        }
        recountMargin(nodes, 0);
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
                    recountMargin(nodes, height);
                }
            }
        });
    }

    private void recountMargin(List<Element> nodes, int height) {
        for (Element node : nodes) {
            String id = node.getAttribute("id");
            Integer result = (Integer) log.getEngine().executeScript("document.getElementById('" + id + "').scrollHeight");
            Integer screenHeight = (Integer) log.getEngine().executeScript("document.getElementById('container').scrollHeight");
            if(height + result >= screenHeight) {
                node.setAttribute("style", "visibility: hidden; bottom: " + height + "px;");
            } else {
                node.setAttribute("style", "visibility: visible; bottom: " + height + "px;");
            }
            height += result + 5;
        }
    }

    class SizeListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if(log.getEngine().getDocument() != null) {
                NodeList prevElements = log.getEngine().getDocument().getElementById("container").getElementsByTagName("div");
                List<Element> nodes = new ArrayList<>();
                for (int i = prevElements.getLength() - 1; i >= 0; i--) {
                    nodes.add((Element) prevElements.item(i));
                }
                recountMargin(nodes, 0);
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
                constructHtml();
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

    private void constructHtml() {
        if(log.getEngine().getDocument() != null) {
            Node html = log.getEngine().getDocument().getElementsByTagName("html").item(0);
            Node body = log.getEngine().getDocument().createElement("body");
            ((Element) body).setAttribute("style", "background : rgba(0,0,0,0);");
            html.appendChild(body);
            Element container = log.getEngine().getDocument().createElement("div");
            container.setAttribute("id", "container");
            body.appendChild(container);
        }
    }
}
