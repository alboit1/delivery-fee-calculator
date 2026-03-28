package com.example.deliveryfee.service;

import com.example.deliveryfee.client.WeatherApiClient;
import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.parser.WeatherXmlParser;
import com.example.deliveryfee.repository.WeatherObservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherImportServiceTest {

    @Mock
    private WeatherApiClient apiClient;

    @Mock
    private WeatherXmlParser xmlParser;

    @Mock
    private WeatherObservationRepository repository;

    @InjectMocks
    private WeatherImportService importService;

    @Test
    void importWeatherData_fetchesParsesAndSaves() {
        String xml = "<xml/>";
        List<WeatherObservation> observations = List.of(new WeatherObservation(), new WeatherObservation());
        when(apiClient.fetchWeatherData()).thenReturn(xml);
        when(xmlParser.parse(xml)).thenReturn(observations);

        importService.importWeatherData();

        verify(repository, times(1)).saveAll(observations);
    }
}
