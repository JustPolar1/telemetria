package com.telemetria.basica.parcel;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelService {

	private final ParcelRepository repository;

	public ParcelService(ParcelRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ParcelResponse create(ParcelRequest request) {
		Parcel parcel = new Parcel(
				request.name().trim(),
				request.location().latitude(),
				request.location().longitude(),
				request.soilType(),
				request.ownerOrLead().trim());
		return ParcelResponse.from(repository.save(parcel));
	}

	@Transactional(readOnly = true)
	public List<ParcelResponse> findAll() {
		return repository.findAll().stream()
				.map(ParcelResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ParcelResponse findById(Long id) {
		return repository.findById(id)
				.map(ParcelResponse::from)
				.orElseThrow(() -> new ParcelNotFoundException(id));
	}
}
