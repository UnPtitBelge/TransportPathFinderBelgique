package com.ulb.util;

public class Utils {

    public static int timeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static int calculateWaitingTime(int currentTimeSeconds, int departureTimeSeconds) {
        if (departureTimeSeconds >= currentTimeSeconds) {
            return departureTimeSeconds - currentTimeSeconds;
        }
        int secondsUntilMidnight = 86400 - currentTimeSeconds;
        return secondsUntilMidnight + departureTimeSeconds;
    }
}
