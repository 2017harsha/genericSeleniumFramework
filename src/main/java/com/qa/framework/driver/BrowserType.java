package com.qa.framework.driver;

import java.util.Locale;

public enum BrowserType {
    CHROME, FIREFOX, EDGE;

    public static BrowserType from(String name) {
        if (name == null || name.isBlank()) {
            return CHROME;
        }
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported browser '" + name + "'. Use chrome, firefox or edge.");
        }
    }
}
