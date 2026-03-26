package com.example.deliveryfee.controller;

import com.example.deliveryfee.service.WeatherImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherImportController {

    private final WeatherImportService weatherImportService;

    public WeatherImportController(WeatherImportService weatherImportService) {
        this.weatherImportService = weatherImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importWeather() {
        weatherImportService.importWeatherData();
        return ResponseEntity.ok("Weather import triggered");
    }
}