package com.telemetria.basica.telemetry;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

	Page<TelemetryReading> findBySensorIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanEqual(
			String sensorId, Instant from, Instant to, Pageable pageable);

	Page<TelemetryReading> findBySensorIdAndMeasuredAtGreaterThanEqual(
			String sensorId, Instant from, Pageable pageable);

	Page<TelemetryReading> findBySensorIdAndMeasuredAtLessThanEqual(
			String sensorId, Instant to, Pageable pageable);

	Page<TelemetryReading> findBySensorId(String sensorId, Pageable pageable);
}
