ALTER TABLE parcels
    ADD COLUMN IF NOT EXISTS sensor_id VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uk_parcels_sensor_id
    ON parcels (sensor_id)
    WHERE sensor_id IS NOT NULL;
