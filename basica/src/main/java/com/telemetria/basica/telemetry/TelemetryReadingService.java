package com.telemetria.basica.telemetry;

import java.time.Instant;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelemetryReadingService {

	private final TelemetryReadingRepository repository;
	private final SensorRepository sensorRepository;

	public TelemetryReadingService(TelemetryReadingRepository repository, SensorRepository sensorRepository) {
		this.repository = repository;
		this.sensorRepository = sensorRepository;
	}

	@Transactional
	public TelemetryReadingResponse receive(TelemetryReadingRequest request) {
		validateMeasurement(request.metric(), request.value());
		String sensorId = request.sensorId().strip();
		Sensor sensor = sensorRepository.findById(sensorId)
				.orElseGet(() -> sensorRepository.save(new Sensor(sensorId)));
		TelemetryReading reading = new TelemetryReading(
				sensor,
				request.metric(),
				request.value(),
				request.measuredAt());
		return TelemetryReadingResponse.from(repository.save(reading));
	}

	private void validateMeasurement(SensorMetric metric, BigDecimal value) {
		boolean valid = switch (metric) {
			case TEMPERATURE -> value.compareTo(BigDecimal.valueOf(-100)) >= 0
					&& value.compareTo(BigDecimal.valueOf(100)) <= 0;
			case HUMIDITY, SOIL_MOISTURE -> value.signum() >= 0
					&& value.compareTo(BigDecimal.valueOf(100)) <= 0;
			case PH -> value.signum() >= 0 && value.compareTo(BigDecimal.valueOf(14)) <= 0;
			case NITROGEN, PHOSPHORUS, POTASSIUM, AIR_QUALITY -> value.signum() >= 0;
		};
		if (!valid) {
			throw new InvalidMeasurementException(metric);
		}
	}

	@Transactional(readOnly = true)
	public Page<TelemetryReadingResponse> findBySensor(
			String sensorId, Instant from, Instant to, int page, int size) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("'from' must be earlier than or equal to 'to'");
		}
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
		}

		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "measuredAt"));
		Page<TelemetryReading> readings;
		if (from != null && to != null) {
			readings = repository.findBySensor_SensorIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanEqual(
					sensorId, from, to, pageable);
		} else if (from != null) {
			readings = repository.findBySensor_SensorIdAndMeasuredAtGreaterThanEqual(sensorId, from, pageable);
		} else if (to != null) {
			readings = repository.findBySensor_SensorIdAndMeasuredAtLessThanEqual(sensorId, to, pageable);
		} else {
			readings = repository.findBySensor_SensorId(sensorId, pageable);
		}
		return readings.map(TelemetryReadingResponse::from);
	}
}
