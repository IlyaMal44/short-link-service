package com.promoit.shortLink.utils;


public class StringUtils {
    /**
     * Обрезает URL до указанной длины с добавлением "..."
     */
    public static String truncateUrl(String url, int maxLength) {
        if (url == null || url.length() <= maxLength) {
            return url;
        }
        return url.substring(0, maxLength - 3) + "...";
    }
}