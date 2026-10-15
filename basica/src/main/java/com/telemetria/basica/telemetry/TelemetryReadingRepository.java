package com.telemetria.basica.telemetry;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

	Page<TelemetryReading> findBySensor_SensorIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanEqual(
			String sensorId, Instant from, Instant to, Pageable pageable);

	Page<TelemetryReading> findBySensor_SensorIdAndMeasuredAtGreaterThanEqual(
			String sensorId, Instant from, Pageable pageable);

	Page<TelemetryReading> findBySensor_SensorIdAndMeasuredAtLessThanEqual(
			String sensorId, Instant to, Pageable pageable);

	Page<TelemetryReading> findBySensor_SensorId(String sensorId, Pageable pageable);

	Optional<TelemetryReading> findFirstBySensor_SensorIdAndMetricOrderByMeasuredAtDescIdDesc(
			String sensorId, SensorMetric metric);
}
