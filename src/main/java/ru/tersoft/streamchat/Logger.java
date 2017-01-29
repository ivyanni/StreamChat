package ru.tersoft.streamchat;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Project streamchat.
 * Created by ivyanni on 22.01.2017.
 */
public class Logger {
    private static TextArea log;

    public static void setLogArea(TextArea textArea) {
        log = textArea;
    }

    public static void printLine(String content) {
        Platform.runLater(() -> {
            if(log != null) {
                log.appendText(getTimeTag() + content + "\n");
                log.positionCaret(log.getText().length());
            }
        });
    }

    private static String getTimeTag() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"));
        return "[" + simpleDateFormat.format(date) + "] ";
    }
}
