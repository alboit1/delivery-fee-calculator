package com.example.deliveryfee.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherPhenomenonClassifierTest {

    @Test
    void isSnowOrSleet_returnsTrueForSnow() {
        assertThat(WeatherPhenomenonClassifier.isSnowOrSleet("Light snow")).isTrue();
        assertThat(WeatherPhenomenonClassifier.isSnowOrSleet("Snow shower")).isTrue();
    }

    @Test
    void isSnowOrSleet_returnsTrueForSleet() {
        assertThat(WeatherPhenomenonClassifier.isSnowOrSleet("Sleet")).isTrue();
    }

    @Test
    void isSnowOrSleet_returnsFalseForRain() {
        assertThat(WeatherPhenomenonClassifier.isSnowOrSleet("Rain")).isFalse();
    }

    @Test
    void isRain_returnsTrueForRain() {
        assertThat(WeatherPhenomenonClassifier.isRain("Rain")).isTrue();
        assertThat(WeatherPhenomenonClassifier.isRain("Light rain")).isTrue();
    }

    @Test
    void isRain_returnsFalseForSnow() {
        assertThat(WeatherPhenomenonClassifier.isRain("Snow")).isFalse();
    }

    @Test
    void isForbidden_returnsTrueForThunder() {
        assertThat(WeatherPhenomenonClassifier.isForbidden("Thunderstorm")).isTrue();
    }

    @Test
    void isForbidden_returnsTrueForHail() {
        assertThat(WeatherPhenomenonClassifier.isForbidden("Hail")).isTrue();
    }

    @Test
    void isForbidden_returnsTrueForGlaze() {
        assertThat(WeatherPhenomenonClassifier.isForbidden("Glaze")).isTrue();
    }

    @Test
    void isForbidden_returnsFalseForNormal() {
        assertThat(WeatherPhenomenonClassifier.isForbidden("Clear")).isFalse();
    }

    @Test
    void nullOrBlankInputsReturnFalse() {
        assertThat(WeatherPhenomenonClassifier.isSnowOrSleet(null)).isFalse();
        assertThat(WeatherPhenomenonClassifier.isSnowOrSleet("")).isFalse();
        assertThat(WeatherPhenomenonClassifier.isRain(null)).isFalse();
        assertThat(WeatherPhenomenonClassifier.isForbidden(null)).isFalse();
    }
}
