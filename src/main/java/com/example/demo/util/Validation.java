package com.example.demo.util;

import com.example.demo.service.BusinessException;

public final class Validation {
    private Validation() {}

    public static String required(String value, String label, int maxLength) {
        return required(value, label, maxLength, null);
    }

    public static String required(String value, String label, int maxLength, String field) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) throw new BusinessException(field, label + "不能为空");
        if (normalized.length() > maxLength) throw new BusinessException(field, label + "不能超过 " + maxLength + " 个字符");
        return normalized;
    }

    public static Long positiveId(String value, String label) {
        return positiveId(value, label, null);
    }

    public static Long positiveId(String value, String label, String field) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new BusinessException(field, label + "无效");
        }
    }
}
