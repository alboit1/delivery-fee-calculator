package com.example.deliveryfee.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.List;

@Component
public class WeatherApiClient {

    private final RestTemplate restTemplate;
    private final String weatherApiUrl;

    public WeatherApiClient(RestTemplateBuilder restTemplateBuilder,
                            @Value("${weather.import.url}") String weatherApiUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.weatherApiUrl = weatherApiUrl;
    }

    public String fetchWeatherData() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                weatherApiUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
        return response.getBody();
    }
}