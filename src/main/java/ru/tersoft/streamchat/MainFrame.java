package ru.tersoft.streamchat;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import ru.tersoft.streamchat.controller.MainController;
import ru.tersoft.streamchat.util.ComponentResizer;
import ru.tersoft.streamchat.util.DataStorage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
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

    private static MainController controller;
    private static JFrame frame;
    private static TrayIcon trayIcon;
    private StackPane root;
    private JFXPanel fxPanel;
    private WebView chatView;
    private ComponentResizer componentResizer;
    private Preferences prefs;
    private ResourceBundle bundle;

    class Delta { double x, y; }

    private MainFrame() {
        prefs = Preferences.userNodeForPackage(getClass());
        String loc = prefs.get(LOCALE, Locale.getDefault().getLanguage());
        Locale.setDefault(new Locale(loc));
        bundle = ResourceBundle.getBundle("locale/strings");
        DataStorage.getDataStorage().setBundle(bundle);
        setAlwaysOnTop(true);
        setType(Type.UTILITY);
        setTitle("Stream Chat");
        setUndecorated(true);
        setFocusable(false);
        setFocusableWindowState(false);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
        chatView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            dragDelta.x = this.getLocation().getX() - event.getScreenX();
            dragDelta.y = this.getLocation().getY() - event.getScreenY();
        });
        chatView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            this.setLocation((int)(event.getScreenX() + dragDelta.x), (int)(event.getScreenY() + dragDelta.y));
        });
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

    private void setDoubleClickListener() {
        chatView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2) {
                    chooseResizeAction(trayIcon.getPopupMenu().getItem(0));
                }
            }
        });
    }

    private Scene createFxScene() throws Exception {
        root = new StackPane();
        createWebView();
        chatView.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                MainFrame.this.setVisible(true);
                createTrayIcon();
                setDoubleClickListener();
            }
        });
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

    private void openSettingsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/layouts/settings.fxml"));
            GridPane root = loader.load();
            Stage settingsStage = new Stage();
            settingsStage.setScene(new Scene(root));
            settingsStage.setTitle(bundle.getString("settings"));
            settingsStage.setResizable(false);
            settingsStage.setWidth(300);
            settingsStage.setHeight(250);
            settingsStage.getIcons().add(new javafx.scene.image.Image
                    (getClass().getResourceAsStream("/icon.png")));
            settingsStage.show();
        } catch(IOException e) {
            e.printStackTrace();
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
            resizeItem.addActionListener(e -> chooseResizeAction(resizeItem));
            popup.add(resizeItem);
            MenuItem reloadItem = new MenuItem(bundle.getString("reload"));
            reloadItem.addActionListener(e -> Platform.runLater(() -> {
                createWebView();
                setDoubleClickListener();
                root.getChildren().add(chatView);
                controller.reload();
            }));
            popup.add(reloadItem);
            MenuItem settingsItem = new MenuItem(bundle.getString("settings"));
            settingsItem.addActionListener(e -> Platform.runLater(this::openSettingsWindow));
            popup.add(settingsItem);
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

    public static void restart() {
        SystemTray.getSystemTray().remove(trayIcon);
        controller.stopAllClients();
        controller = null;
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        frame = new MainFrame();
    }

    public static void main(String[] args) {
        frame = new MainFrame();
    }
}
