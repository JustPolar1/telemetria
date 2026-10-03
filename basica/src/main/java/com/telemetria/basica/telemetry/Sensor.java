package com.telemetria.basica.telemetry;

import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensors")
public class Sensor {

	@Id
	@Column(name = "sensor_id", length = 100)
	private String sensorId;

	@Column(name = "created_at", nullable = false)
	@ColumnDefault("CURRENT_TIMESTAMP")
	private Instant createdAt;

	protected Sensor() {
	}

	public Sensor(String sensorId) {
		this.sensorId = sensorId;
	}

	@PrePersist
	void setCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public String getSensorId() {
		return sensorId;
	}
}
