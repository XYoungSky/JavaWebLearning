package com.example.demo.util;

import com.example.demo.service.BusinessException;

public final class Validation {
    private Validation() {}

    public static String required(String value, String label, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) throw new BusinessException(label + "不能为空");
        if (normalized.length() > maxLength) throw new BusinessException(label + "不能超过 " + maxLength + " 个字符");
        return normalized;
    }

    public static Long positiveId(String value, String label) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new BusinessException(label + "无效");
        }
    }
}
