package com.telemetria.basica.telemetry;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "telemetry_readings")
public class TelemetryReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sensor_id", nullable = false, length = 100)
	private String sensorId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SensorMetric metric;

	@Column(name = "metric_value", nullable = false, precision = 12, scale = 4)
	private BigDecimal value;

	@Column(name = "measured_at", nullable = false)
	private Instant measuredAt;

	@Column(name = "received_at", nullable = false)
	private Instant receivedAt;

	protected TelemetryReading() {
	}

	public TelemetryReading(String sensorId, SensorMetric metric, BigDecimal value, Instant measuredAt) {
		this.sensorId = sensorId;
		this.metric = metric;
		this.value = value;
		this.measuredAt = measuredAt;
	}

	@PrePersist
	void setReceivedAt() {
		if (receivedAt == null) {
			receivedAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public String getSensorId() {
		return sensorId;
	}

	public SensorMetric getMetric() {
		return metric;
	}

	public BigDecimal getValue() {
		return value;
	}

	public Instant getMeasuredAt() {
		return measuredAt;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}
}
