# Delivery Fee Service

Fujitsu Java Programming Task 2026. A Spring Boot application that calculates food courier delivery fees
based on the city, the vehicle type and the current weather.

- Imports weather observations from the [Estonian Environment Agency](https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php)
  on a configurable schedule and keeps their full history.
- Calculates the delivery fee from a regional base fee and weather-based extra fees.
- Stores all fee rules in the database and lets them be managed through a REST API.
- Can calculate the fee for a past moment, using the rules and weather that were valid then.

## Tech stack

Java 21, Spring Boot 4 (Web MVC, Data JPA, Validation, Scheduling), H2 in-memory database,
Jackson XML, Lombok, springdoc-openapi (Swagger UI), JUnit 5, Mockito, AssertJ.

## Running

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` and imports weather data once on startup.

| URL | Description |
|---|---|
| http://localhost:8080/swagger-ui.html | API documentation, try out requests |
| http://localhost:8080/h2-console | Database console (JDBC URL `jdbc:h2:mem:fujitsudb`, user `shrek`, empty password) |

Run the tests:

```bash
./mvnw test
```

## API

### Calculate delivery fee

```
GET /api/delivery-fee?city=TARTU&vehicleType=BIKE
GET /api/delivery-fee?city=TARTU&vehicleType=BIKE&dateTime=2026-01-10T12:00:00Z
```

| Parameter | Required | Values |
|---|---|---|
| `city` | yes | `TALLINN`, `TARTU`, `PARNU` |
| `vehicleType` | yes | `CAR`, `SCOOTER`, `BIKE` |
| `dateTime` | no | ISO-8601 instant, e.g. `2026-01-10T12:00:00Z` or `2026-01-10T14:00:00+02:00`. Defaults to now |

```json
{ "city": "TARTU", "vehicleType": "BIKE", "fee": 4.00 }
```

Errors are returned as [RFC 9457 problem details](https://www.rfc-editor.org/rfc/rfc9457):

| Status | When |
|---|---|
| 400 | Missing or invalid parameter (the message lists allowed values) |
| 404 | No base fee rule for the city and vehicle type |
| 422 | Weather forbids the vehicle: `"Usage of selected vehicle type is forbidden"` |
| 503 | No weather data for the city at the requested time |

```json
{ "status": 422, "title": "Unprocessable Content", "detail": "Usage of selected vehicle type is forbidden", "instance": "/api/delivery-fee" }
```

### Manage fee rules

| Method | URL | Description |
|---|---|---|
| GET | `/api/base-fees`, `/api/extra-fees` | Rules currently in force |
| GET | `/api/base-fees/{id}`, `/api/extra-fees/{id}` | One rule, including closed versions |
| POST | `/api/base-fees`, `/api/extra-fees` | Create a rule, in force from now on |
| PUT | `/api/base-fees/{id}`, `/api/extra-fees/{id}` | Replace a rule with a new version (returns a new id) |
| DELETE | `/api/base-fees/{id}`, `/api/extra-fees/{id}` | End the validity of a rule |

Examples:

```json
POST /api/base-fees
{ "city": "TARTU", "vehicleType": "BIKE", "fee": 2.80 }

POST /api/extra-fees
{ "conditionType": "WIND_SPEED", "vehicleType": "BIKE",
  "minValue": 10, "minInclusive": true, "maxValue": 20, "maxInclusive": true,
  "fee": 0.50, "forbidden": false }

POST /api/extra-fees
{ "conditionType": "WEATHER_PHENOMENON", "vehicleType": "SCOOTER",
  "phenomenonKeyword": "hail", "fee": 0, "forbidden": true }
```

Full request and response schemas are in the Swagger UI.

## Configuration

`src/main/resources/application.properties`:

| Property | Default | Description |
|---|---|---|
| `weather.import.cron` | `0 15 * * * *` | Import schedule, every hour at HH:15:00 |
| `weather.import.url` | Environment Agency observations XML | Weather data source |
| `weather.import.on-startup` | `true` | Import once when the application starts |

## Design

```
controller  → REST endpoints, no business logic
service     → fee calculation, rule management, weather import
repository  → Spring Data JPA queries
entity      → JPA entities: WeatherObservation, RegionalBaseFee, ExtraFeeRule
dto         → request/response records of the REST API
mapper      → conversions between entities, DTOs and the weather XML
client      → HTTP client and XML model of the weather service
scheduler   → cron job for the weather import
exception   → domain exceptions and the global exception handler
model       → enums: City, VehicleType, WeatherConditionType
config      → scheduling, clock, OpenAPI
```

### Database

| Table | Content |
|---|---|
| `weather_observation` | Imported observations. Every import inserts new rows, nothing is ever updated |
| `regional_base_fee` | Base fee per city and vehicle type |
| `extra_fee_rule` | Weather-based extra fee or prohibition per vehicle type |

The initial rules from the task description are loaded from `src/main/resources/data.sql`.

### Rule versioning

Each rule has `validFrom` (inclusive) and `validTo` (exclusive, `null` = in force now). Updating a rule closes the
current version and inserts a new one; deleting a rule only closes it. Together with the weather history, this lets
`dateTime` requests use exactly the rules and weather that were valid at that moment.

### Extra fee rules

An extra fee rule matches either
- a **numeric range** (`AIR_TEMPERATURE`, `WIND_SPEED`): `minValue`/`maxValue`, each optional, with
  `minInclusive`/`maxInclusive` flags, or
- a **keyword** (`WEATHER_PHENOMENON`): the phenomenon text contains `phenomenonKeyword`, case-insensitive.

A matching rule either adds its `fee` or, if `forbidden`, makes the vehicle unavailable.


## Tests

- **Unit tests** (Mockito): fee calculation, rule matching including all boundary values, weather import, mappers.
- **Repository tests** (`@DataJpaTest`): queries for latest observation and rules valid at a moment.
- **Integration tests** (H2 + `data.sql`): every fee rule of the task, the task's example calculation (4 €),
  and that rule changes keep history for `dateTime` requests.
- **Controller tests** (`@WebMvcTest`): HTTP status codes, JSON responses, validation and error messages.
- **Weather client tests** (`MockRestServiceServer`): XML parsing, empty values, encoding and error responses,
  without calling the real service.
