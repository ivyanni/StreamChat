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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = DataStorage.getDataStorage().getBundle();
        setLocalization();
        prefs = Preferences.userNodeForPackage(MainFrame.class);
        username.setText(prefs.get("username", null));
        enableBttv.setSelected(prefs.getBoolean("bttv", true));
        List<String> themeList = new ArrayList<>();
        try {
            InputStream in = getClass().getResourceAsStream("/themes/");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String resource;
            while((resource = br.readLine()) != null) {
                themeList.add(resource.substring(0, resource.indexOf(".")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        localeStringList = new ArrayList<>();
        localeList = new ArrayList<>();
        try {
            InputStream in = getClass().getResourceAsStream("/locale/");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String resource;
            while((resource = br.readLine()) != null) {
                if(!resource.contains("_")) {
                    localeStringList.add(new Locale("en").getDisplayLanguage().toLowerCase());
                    localeList.add(new Locale("en"));
                } else {
                    String localeName = resource.substring("strings_".length(), resource.indexOf("."));
                    localeStringList.add(new Locale(localeName).getDisplayLanguage().toLowerCase());
                    localeList.add(new Locale(localeName));
                }
            }
        } catch (IOException e) {
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
