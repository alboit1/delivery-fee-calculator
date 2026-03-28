# Delivery Fee Calculator

A Spring Boot application that calculates food delivery fees based on city, vehicle type, and real‑time weather conditions.  
Weather data is fetched periodically from the Estonian Environment Agency and stored in an H2 database.

---

## Features

- **REST API** for fee calculation (`GET /api/v1/delivery-fee` and legacy `/delivery-fee`)
- **Scheduled weather import** (default: every hour at minute 15)
- **Business rules** for extra fees based on temperature, wind speed, and weather phenomenon
- **Forbidden vehicle usage** in severe weather (wind >20 m/s, thunder, hail, glaze)
- **Swagger UI** for API documentation
- **In‑memory H2 database** with full history of imported weather observations

---

## Requirements

- Java 17 or later
- Maven (or use the included wrapper `./mvnw`)

---

## Configuration

All settings are in `src/main/resources/application.properties`. Key properties:

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | HTTP port | `8081` |
| `weather.import.url` | URL of the weather API | `https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php` |
| `weather.import.cron` | Cron expression for import | `0 15 * * * *` (every hour at minute 15) |
| `spring.datasource.url` | H2 database URL | `jdbc:h2:mem:deliverydb` |
| `spring.h2.console.enabled` | Enable H2 console | `true` |

---

## Running the Application

1. **Clone the repository** (or copy the source).
2. **Open a terminal** in the project root.
3. **Run the application** with Maven:

   ```bash
   ./mvnw clean spring-boot:run
   ```
Wait for the log message: Started DeliveryFeeApplication in ... seconds. The server is now listening on http://localhost:8081.

---

## H2 Console

To view the database, open http://localhost:8081/h2-console and use:

- JDBC URL: jdbc:h2:mem:deliverydb
- User Name: sa
- Password: (empty)

---

## API Endpoints

### `GET /api/v1/delivery-fee` (or `/delivery-fee`)

Calculates the delivery fee for a given city and vehicle type using the latest weather data.

**Parameters**

| Name          | Type   | Description                                                     | Example                |
|---------------|--------|-----------------------------------------------------------------|------------------------|
| `city`        | string | City name (case‑insensitive, supports diacritics)               | `Tallinn`, `Tartu`, `Pärnu` |
| `vehicleType` | string | Vehicle type (case‑insensitive)                                 | `Car`, `Scooter`, `Bike` |

**Response (success)**

```json
{
  "deliveryFee": 4.0
}
```

**Error responses**
- 400 Bad Request – invalid city/vehicle type, or business rule violation (forbidden vehicle).
Example:
```json
{
  "timestamp": "2026-03-28T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Usage of selected vehicle type is forbidden: wind speed exceeds 20 m/s"
}
```
- 404 Not Found – no weather data available for the city.
Example:
```json
{
  "timestamp": "2026-03-28T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "No weather data available for city: PARNU"
}
```
POST /weather/import (optional, manual trigger)

Triggers an immediate weather data import. Useful for testing or forcing an update.

Response: `Weather import triggered`

## Scheduled Weather Import

The application includes a scheduled task (`WeatherImportScheduler`) that fetches the latest weather data from the Estonian Environment Agency and stores it in the database.

- **Default cron:** `0 15 * * * *` (every hour at minute 15)
- **Configurable via** `weather.import.cron` in `application.properties`
- The import preserves **all history** – each execution adds new rows, never overwrites existing data.

---

## Business Rules

The total fee is the sum of a **regional base fee** and **extra fees** based on weather conditions.

### Base Fees (RBF)

| City       | Car   | Scooter | Bike   |
|------------|-------|---------|--------|
| Tallinn    | 4.00 €| 3.50 €  | 3.00 € |
| Tartu      | 3.50 €| 3.00 €  | 2.50 € |
| Pärnu      | 3.00 €| 2.50 €  | 2.00 € |

### Extra Fees

| Condition                                                      | Vehicle        | Fee    |
|----------------------------------------------------------------|----------------|--------|
| Air temperature < -10°C                                        | Scooter, Bike  | +1.00 € |
| Air temperature between -10°C and 0°C                          | Scooter, Bike  | +0.50 € |
| Wind speed between 10 m/s and 20 m/s                           | Bike only      | +0.50 € |
| Weather phenomenon: snow or sleet                              | Scooter, Bike  | +1.00 € |
| Weather phenomenon: rain                                       | Scooter, Bike  | +0.50 € |

### Forbidden Cases

- **Wind speed > 20 m/s** → bike forbidden (exception thrown)
- **Phenomenon: glaze, hail, thunder** → scooter/bike forbidden (exception thrown)

Extra fees are **only applied for the conditions listed above**; all other conditions incur no extra fee.

---

## Assumptions

1. **Weather API** – The application expects an XML response containing stations, but the actual endpoint (`https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php`) may return plain text. In a production environment, the parser would be adapted to the real format. For this demo, we assume the API provides valid XML.
2. **Station to City mapping** – The following stations are mapped to our cities:
    - `Tallinn-Harku` → Tallinn
    - `Tartu-Tõravere` → Tartu
    - `Pärnu` → Pärnu
3. **Timestamp** – Since the API does not provide a timestamp, the import uses the current system time.
4. **Null safety** – Temperature, wind speed, and phenomenon may be null in the API response. The application treats null values as 0.0 or “no extra fee”.
5. **Error handling** – Any exception during import is logged and the transaction is rolled back; no partial data is saved.
6. **Cron expression** – The default is set to run at minute 15 of every hour, as required. It can be changed via configuration.

---

## Documentation and Testing

- **Swagger UI** is available at `http://localhost:8081/swagger-ui/index.html`
- **H2 Console** at `http://localhost:8081/h2-console`
- **Unit & integration tests** can be run with `./mvnw test`

## Author

Alika Boitšuk - `boitsukalika@gmail.com`