package com.example.demo.service;

import java.sql.SQLException;

final class DatabaseErrors {
    private DatabaseErrors() {}

    static boolean isDuplicateKey(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getErrorCode() == 1062) return true;
        }
        return false;
    }
}
