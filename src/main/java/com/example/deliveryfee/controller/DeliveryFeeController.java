package com.example.deliveryfee.controller;

import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.ForbiddenVehicleException;
import com.example.deliveryfee.exception.WeatherDataNotFoundException;
import com.example.deliveryfee.service.DeliveryFeeService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> calculateFee(@RequestParam String city,
                                          @RequestParam String vehicleType) {
        try {
            City cityEnum = parseCity(city);
            VehicleType vehicleEnum = VehicleType.valueOf(vehicleType.toUpperCase());
            double fee = deliveryFeeService.calculateDeliveryFee(cityEnum, vehicleEnum);
            return ResponseEntity.ok(Map.of("deliveryFee", fee));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid city or vehicle type"));
        } catch (WeatherDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (ForbiddenVehicleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Converts a city name (possibly with diacritics) to the corresponding City enum.
     * Example: "Pärnu" -> PARNU, "Tartu" -> TARTU, "Tallinn" -> TALLINN.
     */
    private City parseCity(String city) {
        // Remove diacritics (e.g., "Pärnu" -> "Parnu")
        String normalized = Normalizer.normalize(city, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return City.valueOf(normalized.toUpperCase());
    }
}