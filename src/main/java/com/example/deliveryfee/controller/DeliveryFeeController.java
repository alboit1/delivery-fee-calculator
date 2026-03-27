package com.example.deliveryfee.controller;

import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.InvalidRequestException;
import com.example.deliveryfee.service.DeliveryFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.Map;

@RestController
@RequestMapping("/delivery-fee")
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> calculateFee(@RequestParam String city,
                                                            @RequestParam String vehicleType) {
        City cityEnum = parseCity(city);
        VehicleType vehicleEnum = parseVehicleType(vehicleType);
        double fee = deliveryFeeService.calculateDeliveryFee(cityEnum, vehicleEnum);
        return ResponseEntity.ok(Map.of("deliveryFee", fee));
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