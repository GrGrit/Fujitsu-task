-- Initial business rules from the task description.
-- All rules are valid from 2000-01-01 with no end date (valid_to = NULL means currently in force).

-- Regional base fees (RBF)
INSERT INTO regional_base_fee (city, vehicle_type, fee, valid_from, valid_to) VALUES
    ('TALLINN', 'CAR',     4.00, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('TALLINN', 'SCOOTER', 3.50, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('TALLINN', 'BIKE',    3.00, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('TARTU',   'CAR',     3.50, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('TARTU',   'SCOOTER', 3.00, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('TARTU',   'BIKE',    2.50, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('PARNU',   'CAR',     3.00, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('PARNU',   'SCOOTER', 2.50, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('PARNU',   'BIKE',    2.00, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL);

-- Extra fees based on air temperature (ATEF), scooter and bike:
--   temperature < -10       -> 1.00
--   -10 <= temperature <= 0 -> 0.50
INSERT INTO extra_fee_rule (condition_type, vehicle_type, min_value, min_inclusive, max_value, max_inclusive, phenomenon_keyword, fee, forbidden, valid_from, valid_to) VALUES
    ('AIR_TEMPERATURE', 'SCOOTER', NULL,  FALSE, -10.00, FALSE, NULL, 1.00, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('AIR_TEMPERATURE', 'SCOOTER', -10.00, TRUE,   0.00, TRUE,  NULL, 0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('AIR_TEMPERATURE', 'BIKE',    NULL,  FALSE, -10.00, FALSE, NULL, 1.00, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('AIR_TEMPERATURE', 'BIKE',    -10.00, TRUE,   0.00, TRUE,  NULL, 0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL);

-- Extra fees based on wind speed (WSEF), bike only:
--   10 <= wind speed <= 20 -> 0.50
--   wind speed > 20        -> vehicle forbidden
INSERT INTO extra_fee_rule (condition_type, vehicle_type, min_value, min_inclusive, max_value, max_inclusive, phenomenon_keyword, fee, forbidden, valid_from, valid_to) VALUES
    ('WIND_SPEED', 'BIKE', 10.00, TRUE,  20.00, TRUE,  NULL, 0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WIND_SPEED', 'BIKE', 20.00, FALSE, NULL,  FALSE, NULL, 0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL);

-- Extra fees based on weather phenomenon (WPEF), scooter and bike:
--   snow or sleet           -> 1.00
--   rain (incl. showers)    -> 0.50
--   glaze, hail, thunder    -> vehicle forbidden
INSERT INTO extra_fee_rule (condition_type, vehicle_type, min_value, min_inclusive, max_value, max_inclusive, phenomenon_keyword, fee, forbidden, valid_from, valid_to) VALUES
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'snow',    1.00, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'sleet',   1.00, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'rain',    0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'shower',  0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'glaze',   0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'hail',    0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'SCOOTER', NULL, FALSE, NULL, FALSE, 'thunder', 0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'snow',    1.00, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'sleet',   1.00, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'rain',    0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'shower',  0.50, FALSE, TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'glaze',   0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'hail',    0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL),
    ('WEATHER_PHENOMENON', 'BIKE',    NULL, FALSE, NULL, FALSE, 'thunder', 0.00, TRUE,  TIMESTAMP WITH TIME ZONE '2000-01-01 00:00:00+00', NULL);
