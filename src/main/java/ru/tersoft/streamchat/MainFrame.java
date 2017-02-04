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
import java.util.prefs.Preferences;

public class MainFrame extends JFrame {
    private final static String XLOC = "x_location";
    private final static String YLOC = "y_location";
    private final static String WIDTH = "window_width";
    private final static String HEIGHT = "window_height";
    private TrayIcon trayIcon;
    private MainController controller;
    private JFXPanel fxPanel;
    private WebView log;
    private ComponentResizer componentResizer;
    private StackPane root;
    private Preferences prefs;

    private MainFrame() {
        prefs = Preferences.userNodeForPackage(getClass());
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

    class Delta { double x, y; }

    private void enableResizeMode() {
        root.setStyle("-fx-background-color: darkred");
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
        componentResizer = new ComponentResizer();
        componentResizer.setMinimumSize(new Dimension(200, 250));
        componentResizer.registerComponent(this);
        final Delta dragDelta = new Delta();
        log.setOnMousePressed(mouseEvent -> {
            dragDelta.x = this.getLocation().getX() - mouseEvent.getScreenX();
            dragDelta.y = this.getLocation().getY() - mouseEvent.getScreenY();
        });
        log.setOnMouseDragged(mouseEvent -> this.setLocation((int)(mouseEvent.getScreenX() + dragDelta.x), (int)(mouseEvent.getScreenY() + dragDelta.y)));
    }

    private void disableResizeMode() {
        root.setStyle("-fx-background-color: transparent");
        getRootPane().setBorder(null);
        componentResizer.deregisterComponent(this);
        log.setOnMousePressed(null);
        log.setOnMouseDragged(null);
        prefs.putDouble(XLOC, getLocation().getX());
        prefs.putDouble(YLOC, getLocation().getY());
        prefs.putInt(WIDTH, getWidth());
        prefs.putInt(HEIGHT, getHeight());
    }

    private Scene createFxScene() throws Exception {
        root = new StackPane();
        log = new WebView();
        log.setVisible(false);
        log.setStyle("-fx-background-color: transparent");
        log.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                MainFrame.this.setVisible(true);
                createTrayIcon();
            }
        });
        Scene scene = new Scene(root);
        root.setStyle("-fx-background-color: transparent");
        root.getChildren().add(log);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        String cssPath = this.getClass().getResource("/darkstyle.css").toExternalForm();
        log.getEngine().setUserStyleSheetLocation(cssPath);
        log.setFocusTraversable(false);
        controller = new MainController(log);
        return scene;
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
        if(resizeItem.getLabel().equals("Resize")) {
            resizeItem.setLabel("Stop resizing");
            enableResizeMode();
        } else {
            resizeItem.setLabel("Resize");
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
            MenuItem resizeItem = new MenuItem("Resize");
            log.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2) {
                        chooseResizeAction(resizeItem);
                    }
                }
            });
            resizeItem.addActionListener(e -> chooseResizeAction(resizeItem));
            popup.add(resizeItem);
            MenuItem reloadItem = new MenuItem("Reload");
            reloadItem.addActionListener(e -> controller.reload());
            popup.add(reloadItem);
            MenuItem closeItem = new MenuItem("Close");
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
