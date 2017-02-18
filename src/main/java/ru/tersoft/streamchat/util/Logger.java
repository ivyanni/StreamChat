package ru.tersoft.streamchat.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.tersoft.streamchat.MainFrame;
import ru.tersoft.streamchat.entity.ChatMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Project streamchat.
 * Created by ivyanni on 22.01.2017.
 */
public class Logger {
    private static final String THEME = "theme";
    private static Logger logger = new Logger();
    private WebView chatView;
    private List<ChatMessage> messages;
    private Preferences prefs;
    private ResourceBundle bundle;

    private Logger() {

    }

    public static Logger getLogger() {
        return logger;
    }

    public void start(WebView log, ResourceBundle bundle) {
        this.chatView = log;
        this.bundle = bundle;
        prefs = Preferences.userNodeForPackage(MainFrame.class);
        this.chatView.setContextMenuEnabled(false);
        createDocument();
        this.chatView.widthProperty().addListener(new SizeListener());
        messages = new ArrayList<>();
    }

    public void reload() {
        Platform.runLater(() -> {
            NodeList nodes = chatView.getEngine().getDocument().getDocumentElement().getChildNodes();
            for(int i = 0; i < nodes.getLength(); i++) {
                chatView.getEngine().getDocument().getDocumentElement().removeChild(nodes.item(i));
            }
            createDocument();
            messages = new ArrayList<>();
        });
    }

    private void deleteMessages(String username) {
        Element container = chatView.getEngine().getDocument().getElementById("container");
        for(ChatMessage message : messages) {
            if(message.getUsername().equals(username)) {
                Element msg = chatView.getEngine().getDocument().getElementById(message.getId());
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
            if(chatView != null) {
                Element editedLine = handleLine(content);
                if (editedLine != null) {
                    if (messages.size() > 8) {
                        Node first = chatView.getEngine().getDocument().getElementById("container").getFirstChild();
                        chatView.getEngine().getDocument().getElementById("container").removeChild(first);
                        messages.remove(0);
                    }
                    NodeList prevElements = chatView.getEngine().getDocument().getElementById("container").getElementsByTagName("div");
                    List<Element> nodes = new ArrayList<>();
                    for (int i = prevElements.getLength() - 1; i >= 0; i--) {
                        nodes.add((Element) prevElements.item(i));
                    }
                    Element element = chatView.getEngine().getDocument().createElement("div");
                    element.setAttribute("class", "well");
                    element.setAttribute("id", editedLine.getAttribute("id"));
                    element.appendChild(editedLine);
                    chatView.getEngine().getDocument().getElementById("container").appendChild(element);
                    String id = element.getAttribute("id");
                    Integer result = (Integer) chatView.getEngine().executeScript("document.getElementById('" + id + "').scrollHeight");
                    int height = result + 5;
                    recountMargin(nodes, height);
                }
            }
        });
    }

    private void recountMargin(List<Element> nodes, int height) {
        for (Element node : nodes) {
            String id = node.getAttribute("id");
            Integer result = (Integer) chatView.getEngine().executeScript("document.getElementById('" + id + "').scrollHeight");
            Integer screenHeight = (Integer) chatView.getEngine().executeScript("document.getElementById('container').scrollHeight");
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
            if(chatView.getEngine().getDocument() != null) {
                NodeList prevElements = chatView.getEngine().getDocument().getElementById("container").getElementsByTagName("div");
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
                if(!line.contains("twitchnotify.tmi.twitch.tv")) {
                    ChatMessage message = new ChatMessage(Calendar.getInstance().getTime(), line);
                    messages.add(message);
                    return message.getElement(chatView.getEngine().getDocument());
                }
            } else if(line.contains("CLEARCHAT")) {
                String username = line.substring(line.indexOf("CLEARCHAT #" + dataStorage.getUsername() + " :")
                        + 13 + dataStorage.getUsername().length());
                deleteMessages(username);
            } else if(line.contains("> :tmi.twitch.tv CAP * ACK :twitch.tv/tags")) {
                Element text = chatView.getEngine().getDocument().createElement("span");
                text.appendChild(chatView.getEngine().getDocument().createTextNode(bundle.getString("finish_connect") + " "));
                Element channelName = chatView.getEngine().getDocument().createElement("strong");
                channelName.setTextContent("#" + dataStorage.getUsername());
                text.appendChild(channelName);
                text.setAttribute("id", "connected");
                return text;
            }
        } else {
            if(line.contains("< PASS")) {
                Element text = chatView.getEngine().getDocument().createElement("span");
                text.appendChild(chatView.getEngine().getDocument().createTextNode(bundle.getString("start_connect") + " "));
                Element channelName = chatView.getEngine().getDocument().createElement("strong");
                channelName.setTextContent("#" + dataStorage.getUsername());
                text.appendChild(channelName);
                text.appendChild(chatView.getEngine().getDocument().createTextNode("..."));
                text.setAttribute("id", "started");
                chatView.setVisible(true);
                return text;
            }
        }
        return null;
    }

    private void createDocument() {
        chatView.getEngine().setJavaScriptEnabled(true);
        chatView.getEngine().loadContent(null);
        chatView.getEngine().loadContent("");
        chatView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == Worker.State.SUCCEEDED) {
                Document document = chatView.getEngine().getDocument();
                Node html = document.createElement("html");
                Node head = document.createElement("head");

                Element link = document.createElement("link");
                link.setAttribute("rel", "stylesheet");
                link.setAttribute("type", "text/css");
                String cssPath = this.getClass().getResource("/themes/default.css").toExternalForm();
                link.setAttribute("href", cssPath);
                head.appendChild(link);

                String theme = prefs.get(THEME, "default");
                if(!theme.equals("default")) {
                    link = document.createElement("link");
                    link.setAttribute("rel", "stylesheet");
                    link.setAttribute("type", "text/css");
                    cssPath = this.getClass().getResource("/themes/" + theme + ".css").toExternalForm();
                    link.setAttribute("href", cssPath);
                    head.appendChild(link);
                }

                html.appendChild(head);
                Node body = document.createElement("body");
                ((Element) body).setAttribute("style", "background : rgba(0,0,0,0);");
                html.appendChild(body);
                Element container = document.createElement("div");
                container.setAttribute("id", "container");
                body.appendChild(container);
                document.getDocumentElement().appendChild(html);
            }
        });
    }
}
