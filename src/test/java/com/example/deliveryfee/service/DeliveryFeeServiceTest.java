package com.example.deliveryfee.service;

import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.BusinessRuleViolationException;
import com.example.deliveryfee.exception.ResourceNotFoundException;
import com.example.deliveryfee.repository.WeatherObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeServiceTest {

    @Mock
    private WeatherObservationRepository repository;

    @InjectMocks
    private DeliveryFeeService service;

    private WeatherObservation sampleObservation;

    @BeforeEach
    void setUp() {
        sampleObservation = new WeatherObservation();
        sampleObservation.setCity(City.TALLINN);
        sampleObservation.setAirTemperature(5.0);
        sampleObservation.setWindSpeed(3.0);
        sampleObservation.setWeatherPhenomenon("Clear");
        sampleObservation.setObservationTimestamp(LocalDateTime.now());
    }

    // Base fee tests
    @Test
    void baseFee_TallinnCar() {
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.CAR);
        assertThat(fee).isEqualTo(4.0); // no extra fees for car
    }

    @Test
    void baseFee_TartuScooter() {
        sampleObservation.setCity(City.TARTU);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TARTU))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TARTU, VehicleType.SCOOTER);
        assertThat(fee).isEqualTo(3.0); // no extra fees because temperature >0, wind moderate, no rain/snow
    }

    @Test
    void baseFee_ParnuBike() {
        sampleObservation.setCity(City.PARNU);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.PARNU))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.PARNU, VehicleType.BIKE);
        assertThat(fee).isEqualTo(2.0); // no extra fees
    }

    // Temperature extra fee
    @Test
    void temperatureExtraFee_ScooterBelowMinus10() {
        sampleObservation.setAirTemperature(-11.0);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER);
        assertThat(fee).isEqualTo(3.5 + 1.0); // base 3.5 + 1
    }

    @Test
    void temperatureExtraFee_BikeBetweenMinus10AndZero() {
        sampleObservation.setAirTemperature(-5.0);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE);
        assertThat(fee).isEqualTo(3.0 + 0.5); // base 3.0 + 0.5
    }

    // Wind extra fee (for bike)
    @Test
    void windExtraFee_BikeWindBetween10And20() {
        sampleObservation.setWindSpeed(15.0);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE);
        assertThat(fee).isEqualTo(3.0 + 0.5); // base + wind fee
    }

    @Test
    void windExtraFee_ScooterIgnoresWind() {
        sampleObservation.setWindSpeed(15.0);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER);
        assertThat(fee).isEqualTo(3.5); // no wind fee for scooter
    }

    // Weather phenomenon extra fee
    @Test
    void phenomenonExtraFee_Snow() {
        sampleObservation.setWeatherPhenomenon("Light snow shower");
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE);
        assertThat(fee).isEqualTo(3.0 + 1.0); // base + snow fee
    }

    @Test
    void phenomenonExtraFee_Rain() {
        sampleObservation.setWeatherPhenomenon("Rain");
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER);
        assertThat(fee).isEqualTo(3.5 + 0.5); // base + rain fee
    }

    // Forbidden cases
    @Test
    void forbidden_WindExceeds20() {
        sampleObservation.setWindSpeed(25.0);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        assertThatThrownBy(() -> service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("wind speed exceeds 20 m/s");
    }

    @Test
    void forbidden_Thunder() {
        sampleObservation.setWeatherPhenomenon("Thunderstorm");
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        assertThatThrownBy(() -> service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("due to severe weather");
    }

    @Test
    void forbidden_Hail() {
        sampleObservation.setWeatherPhenomenon("Hail");
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        assertThatThrownBy(() -> service.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("due to severe weather");
    }

    // Missing weather data
    @Test
    void missingWeatherData_ThrowsResourceNotFound() {
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.calculateDeliveryFee(City.TALLINN, VehicleType.CAR))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No weather data available");
    }

    // Edge cases
    @Test
    void nullAirTemperature_NoExtraFee() {
        sampleObservation.setAirTemperature(null);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER);
        assertThat(fee).isEqualTo(3.5); // base only
    }

    @Test
    void nullWindSpeed_NoWindFee() {
        sampleObservation.setWindSpeed(null);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE);
        assertThat(fee).isEqualTo(3.0); // base only
    }

    @Test
    void nullPhenomenon_NoPhenomenonFee() {
        sampleObservation.setWeatherPhenomenon(null);
        when(repository.findTopByCityOrderByObservationTimestampDesc(City.TALLINN))
                .thenReturn(Optional.of(sampleObservation));
        double fee = service.calculateDeliveryFee(City.TALLINN, VehicleType.BIKE);
        assertThat(fee).isEqualTo(3.0); // base only
    }
}