ALTER TABLE sensor_readings
    DROP CONSTRAINT IF EXISTS sensor_readings_measurement_type_check;

ALTER TABLE sensor_readings
    ADD CONSTRAINT sensor_readings_measurement_type_check
    CHECK (measurement_type IN (
        'temperature', 'humidity', 'soil_moisture', 'ph',
        'nitrogen', 'phosphorus', 'potassium', 'air_quality'
    ));
