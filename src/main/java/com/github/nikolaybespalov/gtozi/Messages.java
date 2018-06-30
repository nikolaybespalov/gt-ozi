package com.github.nikolaybespalov.gtozi;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Messages {
    private final ResourceBundle resourceBundle;

    public Messages(String bundleName) {
        resourceBundle = ResourceBundle.getBundle(bundleName);
    }

    public String format(String key, Object... args) {
        return MessageFormat.format(resourceBundle.getString(key), args);
    }
}
