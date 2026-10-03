package com.telemetria.basica.telemetry;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelemetryReadingService {

	private final TelemetryReadingRepository repository;

	public TelemetryReadingService(TelemetryReadingRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public TelemetryReadingResponse receive(TelemetryReadingRequest request) {
		TelemetryReading reading = new TelemetryReading(
				request.sensorId().trim(),
				request.metric(),
				request.value(),
				request.measuredAt());
		return TelemetryReadingResponse.from(repository.save(reading));
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
			readings = repository.findBySensorIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanEqual(
					sensorId, from, to, pageable);
		} else if (from != null) {
			readings = repository.findBySensorIdAndMeasuredAtGreaterThanEqual(sensorId, from, pageable);
		} else if (to != null) {
			readings = repository.findBySensorIdAndMeasuredAtLessThanEqual(sensorId, to, pageable);
		} else {
			readings = repository.findBySensorId(sensorId, pageable);
		}
		return readings.map(TelemetryReadingResponse::from);
	}
}
