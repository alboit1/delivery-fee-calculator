package com.example.deliveryfee.service;

import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.BusinessRuleViolationException;
import com.example.deliveryfee.exception.ResourceNotFoundException;
import com.example.deliveryfee.repository.WeatherObservationRepository;
import com.example.deliveryfee.util.WeatherPhenomenonClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for calculating delivery fees based on city, vehicle type, and latest weather data.
 */
@Service
public class DeliveryFeeService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryFeeService.class);
    private static final String FORBIDDEN_MESSAGE_WIND = "Usage of selected vehicle type is forbidden: wind speed exceeds 20 m/s";
    private static final String FORBIDDEN_MESSAGE_WEATHER = "Usage of selected vehicle type is forbidden due to severe weather: ";

    private final WeatherObservationRepository weatherObservationRepository;

    public DeliveryFeeService(WeatherObservationRepository weatherObservationRepository) {
        this.weatherObservationRepository = weatherObservationRepository;
    }

    /**
     * Calculates the total delivery fee for a given city and vehicle type using the latest weather data.
     */
    public double calculateDeliveryFee(City city, VehicleType vehicleType) {
        WeatherObservation latestWeather = getLatestWeather(city);

        double baseFee = getBaseFee(city, vehicleType);
        double extraFees = 0.0;

        if (vehicleType == VehicleType.SCOOTER || vehicleType == VehicleType.BIKE) {
            extraFees += getTemperatureExtraFee(latestWeather.getAirTemperature());
            extraFees += getWeatherPhenomenonExtraFee(latestWeather.getWeatherPhenomenon());
            if (vehicleType == VehicleType.BIKE) {
                extraFees += getWindExtraFee(latestWeather.getWindSpeed());
            }
        }

        double totalFee = baseFee + extraFees;
        log.info(
                "Calculated delivery fee. city={}, vehicleType={}, baseFee={}, extraFees={}, totalFee={}",
                city, vehicleType, baseFee, extraFees, totalFee
        );
        return totalFee;
    }

    private WeatherObservation getLatestWeather(City city) {
        return weatherObservationRepository.findTopByCityOrderByObservationTimestampDesc(city)
                .orElseThrow(() -> new ResourceNotFoundException("No weather data available for city: " + city));
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
            throw new BusinessRuleViolationException(FORBIDDEN_MESSAGE_WIND);
        }
        if (windSpeed >= 10.0 && windSpeed <= 20.0) return 0.5;
        return 0.0;
    }

    private double getWeatherPhenomenonExtraFee(String phenomenon) {
        if (phenomenon == null || phenomenon.isBlank()) return 0.0;
        if (WeatherPhenomenonClassifier.isForbidden(phenomenon)) {
            throw new BusinessRuleViolationException(FORBIDDEN_MESSAGE_WEATHER + phenomenon);
        }
        if (WeatherPhenomenonClassifier.isSnowOrSleet(phenomenon)) return 1.0;
        if (WeatherPhenomenonClassifier.isRain(phenomenon)) return 0.5;
        return 0.0;
    }
}
