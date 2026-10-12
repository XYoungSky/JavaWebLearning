package com.example.demo.service;

public class BusinessException extends RuntimeException {
    private final String field;

    public BusinessException(String message) { this(null, message, null); }
    public BusinessException(String message, Throwable cause) { this(null, message, cause); }
    public BusinessException(String field, String message) { this(field, message, null); }
    public BusinessException(String field, String message, Throwable cause) {
        super(message, cause);
        this.field = field;
    }
    public String getField() { return field; }
}
