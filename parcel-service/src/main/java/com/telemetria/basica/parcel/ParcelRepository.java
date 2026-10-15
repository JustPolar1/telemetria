package com.telemetria.basica.parcel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
	boolean existsBySensorId(String sensorId);

	boolean existsBySensorIdAndIdNot(String sensorId, Long id);
}
