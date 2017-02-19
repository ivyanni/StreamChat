package ru.tersoft.streamchat.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ru.tersoft.streamchat.MainFrame;
import ru.tersoft.streamchat.util.DataStorage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;

/**
 * Project streamchat.
 * Created by ivyanni on 18.02.2017.
 */
public class SettingsController implements Initializable {
    @FXML
    private Label username;
    @FXML
    private CheckBox enableBttv;
    @FXML
    private ComboBox<String> themes;
    @FXML
    private ComboBox<String> locales;
    @FXML
    private Button logoutButton;
    @FXML
    private Button saveSettings;
    @FXML
    private Button cancelButton;
    @FXML
    private Label authorizedText;
    @FXML
    private Label themeText;
    @FXML
    private Label languageText;

    private Preferences prefs;
    private List<Locale> localeList;
    private List<String> localeStringList;
    private ResourceBundle bundle;

    public SettingsController() {
    }

    static <T extends Collection<? super String>> T getFileListing(
            final Class<?> clazz, final String path, final T result) throws URISyntaxException,
            IOException {
        URL dirURL = clazz.getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            result.addAll(Arrays.asList(new File(dirURL.toURI()).list()));
            return result;
        }
        if (dirURL == null) {
            final String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getResource(me);
        }
        if (dirURL.getProtocol().equals("jar")) {
            final String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
            final JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                final int pathIndex = name.lastIndexOf(path);
                if (pathIndex > 0) {
                    final String nameWithPath = name.substring(name.lastIndexOf(path));
                    result.add(nameWithPath.substring(path.length()));
                }
            }
            jar.close();
            return result;
        }
        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = DataStorage.getDataStorage().getBundle();
        setLocalization();
        prefs = Preferences.userNodeForPackage(MainFrame.class);
        username.setText(prefs.get("username", null));
        enableBttv.setSelected(prefs.getBoolean("bttv", true));
        List<String> themeList = new ArrayList<>();
        try {
            for (final String resource : getFileListing(MainFrame.class,"themes/", new HashSet<>())) {
                if(!resource.isEmpty()) {
                    themeList.add(resource.substring(0, resource.indexOf(".")));
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        localeStringList = new ArrayList<>();
        localeList = new ArrayList<>();
        try {
            for (final String resource : getFileListing(MainFrame.class,"locale/", new HashSet<>())) {
                if(!resource.isEmpty()) {
                    if(!resource.contains("_")) {
                        localeStringList.add(new Locale("en").getDisplayLanguage().toLowerCase());
                        localeList.add(new Locale("en"));
                    } else {
                        String localeName = resource.substring("strings_".length(), resource.indexOf("."));
                        localeStringList.add(new Locale(localeName).getDisplayLanguage().toLowerCase());
                        localeList.add(new Locale(localeName));
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        themes.setItems(FXCollections.observableArrayList(themeList));
        locales.setItems(FXCollections.observableArrayList(localeStringList));
        themes.setValue(prefs.get("theme", "default"));
        locales.setValue(new Locale(prefs.get("locale", "en")).getDisplayLanguage().toLowerCase());
        username.setText(prefs.get("username", null));
        cancelButton.onActionProperty().setValue(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
        logoutButton.onActionProperty().setValue(event -> {
            prefs.remove("access_token");
            prefs.remove("username");
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.close();
            MainFrame.restart();
        });
        saveSettings.onActionProperty().setValue(event -> {
            prefs.putBoolean("bttv", enableBttv.isSelected());
            prefs.put("theme", themes.getValue());
            int localeIndex = localeStringList.indexOf(locales.getValue());
            prefs.put("locale", localeList.get(localeIndex).getLanguage());
            Stage stage = (Stage) saveSettings.getScene().getWindow();
            stage.close();
            MainFrame.restart();
        });
    }

    private void setLocalization() {
        authorizedText.setText(bundle.getString("authorized_as"));
        themeText.setText(bundle.getString("theme"));
        languageText.setText(bundle.getString("language"));
        logoutButton.setText(bundle.getString("logout"));
        saveSettings.setText(bundle.getString("save_restart"));
        cancelButton.setText(bundle.getString("cancel"));
        enableBttv.setText(bundle.getString("enable_bttv"));
    }
}
