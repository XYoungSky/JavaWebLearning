package com.example.demo.util;

import java.util.Set;

public final class Search {
    private Search() {}

    public static String keyword(String raw) {
        String value = raw == null ? "" : raw.trim();
        return value.length() > 120 ? value.substring(0, 120) : value;
    }

    public static String status(String raw, String... allowed) {
        return raw != null && Set.of(allowed).contains(raw) ? raw : "";
    }

    /** Treat user-entered %, _ and ! literally, with an explicit SQL LIKE escape character. */
    public static String pattern(String keyword) {
        return "%" + keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }
}
