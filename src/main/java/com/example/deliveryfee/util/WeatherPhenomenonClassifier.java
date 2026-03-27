package com.example.deliveryfee.util;

public final class WeatherPhenomenonClassifier {

    private WeatherPhenomenonClassifier() {
    }

    public static boolean isSnowOrSleet(String phenomenon) {
        if (phenomenon == null || phenomenon.isBlank()) {
            return false;
        }

        String value = phenomenon.toLowerCase();
        return value.contains("snow") || value.contains("sleet");
    }

    public static boolean isRain(String phenomenon) {
        if (phenomenon == null || phenomenon.isBlank()) {
            return false;
        }

        String value = phenomenon.toLowerCase();
        return value.contains("rain");
    }

    public static boolean isForbidden(String phenomenon) {
        if (phenomenon == null || phenomenon.isBlank()) {
            return false;
        }

        String value = phenomenon.toLowerCase();
        return value.contains("glaze")
                || value.contains("hail")
                || value.contains("thunder");
    }
}
