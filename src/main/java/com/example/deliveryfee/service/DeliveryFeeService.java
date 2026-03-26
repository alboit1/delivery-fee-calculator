package com.example.deliveryfee.service;

import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.ForbiddenVehicleException;
import com.example.deliveryfee.exception.WeatherDataNotFoundException;
import com.example.deliveryfee.repository.WeatherObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for calculating delivery fees based on city, vehicle type, and latest weather data.
 */
@Service
public class DeliveryFeeService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryFeeService.class);

    private final WeatherObservationRepository weatherObservationRepository;

    public DeliveryFeeService(WeatherObservationRepository weatherObservationRepository) {
        this.weatherObservationRepository = weatherObservationRepository;
    }

    /**
     * Calculates the total delivery fee for a given city and vehicle type using the latest weather data.
     *
     * @param city        the city (Tallinn, Tartu, Pärnu)
     * @param vehicleType the vehicle type (Car, Scooter, Bike)
     * @return total delivery fee as double
     * @throws ForbiddenVehicleException if usage of the vehicle is forbidden due to weather conditions
     * @throws IllegalArgumentException  if no weather data is found for the city
     */
    public double calculateDeliveryFee(City city, VehicleType vehicleType) {
        WeatherObservation weather = getLatestWeather(city);

        double baseFee = getBaseFee(city, vehicleType);

        double extraFees = 0.0;
        if (vehicleType == VehicleType.SCOOTER || vehicleType == VehicleType.BIKE) {
            extraFees += getTemperatureExtraFee(weather.getAirTemperature());
            extraFees += getWeatherPhenomenonExtraFee(weather.getWeatherPhenomenon(), vehicleType);
            if (vehicleType == VehicleType.BIKE) {
                extraFees += getWindExtraFee(weather.getWindSpeed());
            }
        }

        double total = baseFee + extraFees;
        log.info("Calculated fee for {} using {}: base={}, extra={}, total={}",
                city, vehicleType, baseFee, extraFees, total);
        return total;
    }

    private WeatherObservation getLatestWeather(City city) {
        Optional<WeatherObservation> latest = weatherObservationRepository
                .findTopByCityOrderByObservationTimestampDesc(city);
        if (latest.isEmpty()) {
            throw new WeatherDataNotFoundException("No weather data available for city: " + city);
        }
        return weatherObservationRepository
                .findTopByCityOrderByObservationTimestampDesc(city)
                .orElseThrow(() -> new WeatherDataNotFoundException("No weather data for " + city));
    }

    private double getBaseFee(City city, VehicleType vehicleType) {
        return switch (city) {
            case TALLINN -> switch (vehicleType) {
                case CAR -> 4.0;
                case SCOOTER -> 3.5;
                case BIKE -> 3.0;
            };
            case TARTU -> switch (vehicleType) {
                case CAR -> 3.5;
                case SCOOTER -> 3.0;
                case BIKE -> 2.5;
            };
            case PARNU -> switch (vehicleType) {
                case CAR -> 3.0;
                case SCOOTER -> 2.5;
                case BIKE -> 2.0;
            };
        };
    }

    private double getTemperatureExtraFee(Double airTemperature) {
        if (airTemperature == null) return 0.0;
        if (airTemperature < -10.0) return 1.0;
        if (airTemperature < 0.0) return 0.5;
        return 0.0;
    }

    private double getWindExtraFee(Double windSpeed) {
        if (windSpeed == null) return 0.0;
        if (windSpeed > 20.0) {
            throw new ForbiddenVehicleException("Usage of selected vehicle type is forbidden: wind speed exceeds 20 m/s");
        }
        if (windSpeed >= 10.0 && windSpeed <= 20.0) return 0.5;
        return 0.0;
    }

    private double getWeatherPhenomenonExtraFee(String phenomenon, VehicleType vehicleType) {
        if (phenomenon == null || phenomenon.isBlank()) return 0.0;

        String lowerPhenom = phenomenon.toLowerCase();

        if (lowerPhenom.contains("glaze") || lowerPhenom.contains("hail") || lowerPhenom.contains("thunder")) {
            throw new ForbiddenVehicleException("Usage of selected vehicle type is forbidden due to severe weather: " + phenomenon);
        }

        if (lowerPhenom.contains("snow") || lowerPhenom.contains("sleet")) {
            return 1.0;
        }
        if (lowerPhenom.contains("rain")) {
            return 0.5;
        }
        return 0.0;
    }
}
