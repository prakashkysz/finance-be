package com.finance.finance.util;

public final class MaskingUtils {

    private MaskingUtils() {
    }

    /** ABCDE1234F becomes XXXXXX234F. */
    public static String maskPan(String pan) {
        return "X".repeat(pan.length() - 4) + pan.substring(pan.length() - 4);
    }
}
