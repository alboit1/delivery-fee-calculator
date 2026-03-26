package com.example.deliveryfee.service;

import com.example.deliveryfee.client.WeatherApiClient;
import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.parser.WeatherXmlParser;
import com.example.deliveryfee.repository.WeatherObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WeatherImportService {

    private static final Logger log = LoggerFactory.getLogger(WeatherImportService.class);

    private final WeatherApiClient weatherApiClient;
    private final WeatherXmlParser weatherXmlParser;
    private final WeatherObservationRepository weatherObservationRepository;

    public WeatherImportService(WeatherApiClient weatherApiClient,
                                WeatherXmlParser weatherXmlParser,
                                WeatherObservationRepository weatherObservationRepository) {
        this.weatherApiClient = weatherApiClient;
        this.weatherXmlParser = weatherXmlParser;
        this.weatherObservationRepository = weatherObservationRepository;
    }

    @Transactional
    public void importWeatherData() {
        log.info("Starting weather data import...");
        try {
            String xmlData = weatherApiClient.fetchWeatherData();
            List<WeatherObservation> observations = weatherXmlParser.parse(xmlData);
            log.info("Parsed {} observations", observations.size());

            // Save all as new records (always INSERT)
            weatherObservationRepository.saveAll(observations);
            log.info("Successfully imported {} weather observations", observations.size());
        } catch (Exception e) {
            log.error("Failed to import weather data", e);
            throw new RuntimeException("Weather import failed", e);
        }
    }
}