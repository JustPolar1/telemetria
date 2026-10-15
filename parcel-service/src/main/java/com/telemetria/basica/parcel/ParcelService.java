package com.telemetria.basica.parcel;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelService {

	private final ParcelRepository repository;
	private final TelemetryClient telemetryClient;

	public ParcelService(ParcelRepository repository, TelemetryClient telemetryClient) {
		this.repository = repository;
		this.telemetryClient = telemetryClient;
	}

	@Transactional
	public ParcelResponse create(ParcelRequest request) {
		String sensorId = request.sensorId().strip();
		if (repository.existsBySensorId(sensorId)) {
			throw new DuplicateSensorAssignmentException(sensorId);
		}
		Parcel parcel = new Parcel(
				request.name().strip(),
				request.location().latitude(),
				request.location().longitude(),
				request.soilType(),
				request.ownerOrLead().strip(),
				sensorId);
		return ParcelResponse.from(repository.save(parcel));
	}

	@Transactional(readOnly = true)
	public List<ParcelResponse> findAll() {
		return repository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
				.map(ParcelResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ParcelResponse findById(Long id) {
		return repository.findById(id)
				.map(ParcelResponse::from)
				.orElseThrow(() -> new ParcelNotFoundException(id));
	}

	public ParcelDetailsResponse findDetailsById(Long id) {
		Parcel parcel = repository.findById(id)
				.orElseThrow(() -> new ParcelNotFoundException(id));
		ParcelResponse response = ParcelResponse.from(parcel);
		if (parcel.getSensorId() == null) {
			return ParcelDetailsResponse.from(response, "UNASSIGNED", List.of());
		}

		List<TelemetryReadingSnapshot> readings = telemetryClient.findLatestReadings(parcel.getSensorId());
		return ParcelDetailsResponse.from(response, readings.isEmpty() ? "NO_DATA" : "AVAILABLE", readings);
	}

	@Transactional
	public ParcelResponse update(Long id, ParcelRequest request) {
		Parcel parcel = repository.findById(id)
				.orElseThrow(() -> new ParcelNotFoundException(id));
		String sensorId = request.sensorId().strip();
		if (!sensorId.equals(parcel.getSensorId()) && repository.existsBySensorIdAndIdNot(sensorId, id)) {
			throw new DuplicateSensorAssignmentException(sensorId);
		}
		parcel.update(
				request.name().strip(),
				request.location().latitude(),
				request.location().longitude(),
				request.soilType(),
				request.ownerOrLead().strip(),
				sensorId);
		return ParcelResponse.from(repository.save(parcel));
	}

	@Transactional
	public void delete(Long id) {
		Parcel parcel = repository.findById(id)
				.orElseThrow(() -> new ParcelNotFoundException(id));
		repository.delete(parcel);
	}
}
