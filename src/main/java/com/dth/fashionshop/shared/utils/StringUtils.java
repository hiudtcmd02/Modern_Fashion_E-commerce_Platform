package com.dth.fashionshop.shared.utils;

public final class StringUtils {

    private StringUtils() {}

    // Hàm chuẩn hóa hoặc tạo slug
    public static String generateSlug(String input) {
        if (input == null) return null;
        return input.trim().toLowerCase().replaceAll("\\s+", "-");
    }

    // Hàm chuẩn hóa mã Code (SKU, Mã đơn hàng, Mã giảm giá...)
    public static String normalizeCode(String code) {
        if (code == null) return null;
        return code.trim().toUpperCase();
    }
}