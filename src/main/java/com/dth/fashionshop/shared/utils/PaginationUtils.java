package com.dth.fashionshop.shared.utils;

public final class PaginationUtils {

    private PaginationUtils() {}

    public static int correctPageNo(int page) {
        return page > 0 ? page - 1 : 0;
    }
}