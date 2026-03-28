package com.example.deliveryfee.controller;

import com.example.deliveryfee.dto.DeliveryFeeResponse;
import com.example.deliveryfee.enums.City;
import com.example.deliveryfee.enums.VehicleType;
import com.example.deliveryfee.exception.InvalidRequestException;
import com.example.deliveryfee.service.DeliveryFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;

@RestController
@RequestMapping("/delivery-fee")
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    @GetMapping
    @Operation(summary = "Calculate delivery fee", description = "Returns total delivery fee based on city and vehicle type, using the latest weather data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful calculation",
                    content = @Content(schema = @Schema(implementation = DeliveryFeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation"),
            @ApiResponse(responseCode = "404", description = "No weather data found for the city")
    })
    public ResponseEntity<DeliveryFeeResponse> calculateFee(
            @Parameter(description = "City name (Tallinn, Tartu, Pärnu)", example = "Tallinn", required = true)
            @RequestParam String city,
            @Parameter(description = "Vehicle type (Car, Scooter, Bike)", example = "Bike", required = true)
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