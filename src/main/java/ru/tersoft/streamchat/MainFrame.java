package ru.tersoft.streamchat;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import ru.tersoft.streamchat.controller.MainController;
import ru.tersoft.streamchat.util.ComponentResizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainFrame extends JFrame {
    private final static String XLOC = "x_location";
    private final static String YLOC = "y_location";
    private final static String WIDTH = "window_width";
    private final static String HEIGHT = "window_height";
    private final static String LOCALE = "locale";

    private TrayIcon trayIcon;
    private MainController controller;
    private JFXPanel fxPanel;
    private WebView chatView;
    private ComponentResizer componentResizer;
    private StackPane root;
    private Preferences prefs;
    private ResourceBundle bundle;

    class Delta { double x, y; }

    private MainFrame() {
        prefs = Preferences.userNodeForPackage(getClass());
        String loc = prefs.get(LOCALE, Locale.getDefault().getLanguage());
        bundle = ResourceBundle.getBundle("locale/strings", new Locale(loc));
        setAlwaysOnTop(true);
        setType(Type.UTILITY);
        setUndecorated(true);
        setFocusable(false);
        setFocusableWindowState(false);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(getClass().getResourceAsStream("/icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSize(prefs.getInt(WIDTH, 250), prefs.getInt(HEIGHT, 400));
        setLocation((int)prefs.getDouble(XLOC, 0), (int)prefs.getDouble(YLOC, 0));
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(() -> {
            try {
                fxPanel.setScene(createFxScene());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void enableResizeMode() {
        root.setStyle("-fx-background-color: lightgrey");
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
        componentResizer = new ComponentResizer();
        componentResizer.setMinimumSize(new Dimension(200, 250));
        componentResizer.registerComponent(this);
        final Delta dragDelta = new Delta();
        chatView.setOnMousePressed(mouseEvent -> {
            dragDelta.x = this.getLocation().getX() - mouseEvent.getScreenX();
            dragDelta.y = this.getLocation().getY() - mouseEvent.getScreenY();
        });
        chatView.setOnMouseDragged(mouseEvent -> this.setLocation((int)(mouseEvent.getScreenX() + dragDelta.x), (int)(mouseEvent.getScreenY() + dragDelta.y)));
    }

    private void disableResizeMode() {
        root.setStyle("-fx-background-color: transparent");
        getRootPane().setBorder(null);
        componentResizer.deregisterComponent(this);
        chatView.setOnMousePressed(null);
        chatView.setOnMouseDragged(null);
        prefs.putDouble(XLOC, getLocation().getX());
        prefs.putDouble(YLOC, getLocation().getY());
        prefs.putInt(WIDTH, getWidth());
        prefs.putInt(HEIGHT, getHeight());
    }

    private Scene createFxScene() throws Exception {
        root = new StackPane();
        createWebView();
        Scene scene = new Scene(root);
        root.setStyle("-fx-background-color: transparent");
        root.getChildren().add(chatView);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        controller = new MainController(chatView, bundle);
        return scene;
    }

    private void createWebView() {
        chatView = new WebView();
        chatView.setVisible(false);
        chatView.setStyle("-fx-background-color: transparent");
        chatView.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                MainFrame.this.setVisible(true);
                createTrayIcon();
            }
        });
        chatView.setFocusTraversable(false);
        chatView.getEngine().getHistory().setMaxSize(0);
    }

    private final ActionListener closeListener = e -> Platform.runLater(new Runnable() {
        @Override
        public void run() {
            SystemTray.getSystemTray().remove(trayIcon);
            controller.stopAllClients();
            Platform.exit();
            System.exit(0);
        }
    });

    private void chooseResizeAction(MenuItem resizeItem) {
        if(resizeItem.getLabel().equals(bundle.getString("resize"))) {
            resizeItem.setLabel(bundle.getString("stop_resize"));
            enableResizeMode();
        } else {
            resizeItem.setLabel(bundle.getString("resize"));
            disableResizeMode();
        }
    }

    private void createTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = null;
            try {
                image = ImageIO.read(getClass().getResourceAsStream("/tray.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PopupMenu popup = new PopupMenu();
            MenuItem resizeItem = new MenuItem(bundle.getString("resize"));
            chatView.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2) {
                        chooseResizeAction(resizeItem);
                    }
                }
            });
            resizeItem.addActionListener(e -> chooseResizeAction(resizeItem));
            popup.add(resizeItem);
            MenuItem reloadItem = new MenuItem(bundle.getString("reload"));
            reloadItem.addActionListener(e -> Platform.runLater(() -> {
                createWebView();
                root.getChildren().add(chatView);
                controller.reload();
            }));
            popup.add(reloadItem);
            MenuItem closeItem = new MenuItem(bundle.getString("exit"));
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            if(image != null) {
                trayIcon = new TrayIcon(image, "Stream Chat", popup);
            } else throw new NullPointerException();
            try {
                tray.add(trayIcon);
            } catch (AWTException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        PlatformImpl.setTaskbarApplication(false);
        new MainFrame();
    }
}
