package com.telemetria.basica.parcel;

import java.util.List;

import org.springframework.data.domain.Sort;
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
				request.name().strip(),
				request.location().latitude(),
				request.location().longitude(),
				request.soilType(),
				request.ownerOrLead().strip());
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

	@Transactional
	public ParcelResponse update(Long id, ParcelRequest request) {
		Parcel parcel = repository.findById(id)
				.orElseThrow(() -> new ParcelNotFoundException(id));
		parcel.update(
				request.name().strip(),
				request.location().latitude(),
				request.location().longitude(),
				request.soilType(),
				request.ownerOrLead().strip());
		return ParcelResponse.from(repository.save(parcel));
	}

	@Transactional
	public void delete(Long id) {
		Parcel parcel = repository.findById(id)
				.orElseThrow(() -> new ParcelNotFoundException(id));
		repository.delete(parcel);
	}
}
