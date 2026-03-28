package com.example.deliveryfee.controller;

import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.repository.WeatherObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryFeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeatherObservationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private void insertWeather(City city, double temp, double wind, String phenomenon) {
        WeatherObservation obs = new WeatherObservation();
        obs.setCity(city);
        obs.setAirTemperature(temp);
        obs.setWindSpeed(wind);
        obs.setWeatherPhenomenon(phenomenon);
        obs.setObservationTimestamp(LocalDateTime.now());
        obs.setStationName(city == City.TALLINN ? "Tallinn-Harku" :
                city == City.TARTU ? "Tartu-Tõravere" : "Pärnu");
        obs.setWmoCode("12345");
        repository.save(obs);
    }

    @Test
    void testTallinnCar() throws Exception {
        insertWeather(City.TALLINN, 5.0, 3.0, "Clear");
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Tallinn")
                        .param("vehicleType", "Car"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryFee").value(4.0));
    }

    @Test
    void testTartuScooterWithTemperatureExtra() throws Exception {
        insertWeather(City.TARTU, -5.0, 3.0, "Clear");
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Tartu")
                        .param("vehicleType", "Scooter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryFee").value(3.0 + 0.5)); // base 3.0 + temp fee 0.5
    }

    @Test
    void testParnuBikeWithAllExtras() throws Exception {
        insertWeather(City.PARNU, -5.0, 15.0, "Rain");
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Pärnu")
                        .param("vehicleType", "Bike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryFee").value(2.0 + 0.5 + 0.5 + 0.5)); // base + temp + wind + rain
    }

    @Test
    void testForbiddenWind() throws Exception {
        insertWeather(City.TALLINN, 5.0, 25.0, "Clear");
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Tallinn")
                        .param("vehicleType", "Bike"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usage of selected vehicle type is forbidden: wind speed exceeds 20 m/s"));
    }

    @Test
    void testForbiddenThunder() throws Exception {
        insertWeather(City.TARTU, 5.0, 3.0, "Thunderstorm");
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Tartu")
                        .param("vehicleType", "Scooter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usage of selected vehicle type is forbidden due to severe weather: Thunderstorm"));
    }

    @Test
    void testCityWithoutWeatherData() throws Exception {
        // No data inserted for Pärnu
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Pärnu")
                        .param("vehicleType", "Car"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No weather data available for city: PARNU"));
    }

    @Test
    void testInvalidCity() throws Exception {
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "InvalidCity")
                        .param("vehicleType", "Car"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid city: InvalidCity"));
    }

    @Test
    void testInvalidVehicleType() throws Exception {
        mockMvc.perform(get("/delivery-fee")
                        .param("city", "Tallinn")
                        .param("vehicleType", "Plane"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid vehicle type: Plane"));
    }
}
