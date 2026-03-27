package com.example.deliveryfee.controller;

import com.example.deliveryfee.dto.DeliveryFeeResponse;
import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.InvalidRequestException;
import com.example.deliveryfee.service.DeliveryFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;

/**
 * REST controller for delivery fee calculation (version 1).
 * Endpoint: GET /api/v1/delivery-fee?city={city}&vehicleType={type}
 */
@RestController
@RequestMapping("/api/v1/delivery-fee")
public class DeliveryFeeRestController {

    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeRestController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    /**
     * Calculates the delivery fee based on city and vehicle type.
     *
     * @param city        city name (case-insensitive, supports diacritics, e.g., "Pärnu")
     * @param vehicleType vehicle type (case-insensitive, e.g., "Bike")
     * @return DeliveryFeeResponse containing the calculated fee
     * @throws InvalidRequestException if city or vehicle type is invalid
     */
    @GetMapping
    public ResponseEntity<DeliveryFeeResponse> calculateFee(
            @RequestParam String city,
            @RequestParam String vehicleType) {

        City cityEnum = parseCity(city);
        VehicleType vehicleEnum = parseVehicleType(vehicleType);
        double fee = deliveryFeeService.calculateDeliveryFee(cityEnum, vehicleEnum);
        return ResponseEntity.ok(new DeliveryFeeResponse(fee));
    }

    private City parseCity(String city) {
        try {
            String normalized = Normalizer.normalize(city, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            return City.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid city: " + city);
        }
    }

    private VehicleType parseVehicleType(String vehicleType) {
        try {
            return VehicleType.valueOf(vehicleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid vehicle type: " + vehicleType);
        }
    }
}
