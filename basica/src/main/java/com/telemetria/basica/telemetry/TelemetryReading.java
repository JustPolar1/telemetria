package com.telemetria.basica.telemetry;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensor_readings")
public class TelemetryReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@Convert(converter = SensorMetricConverter.class)
	@Column(name = "measurement_type", nullable = false, length = 20)
	private SensorMetric metric;

	@Column(name = "measured_value", nullable = false, precision = 12, scale = 4)
	private BigDecimal value;

	@Column(name = "measured_at", nullable = false)
	private Instant measuredAt;

	@Generated(event = EventType.INSERT)
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "recorded_at", nullable = false, insertable = false, updatable = false)
	private Instant recordedAt;

	protected TelemetryReading() {
	}

	public TelemetryReading(Sensor sensor, SensorMetric metric, BigDecimal value, Instant measuredAt) {
		this.sensor = sensor;
		this.metric = metric;
		this.value = value;
		this.measuredAt = measuredAt;
	}

	public Long getId() {
		return id;
	}

	public String getSensorId() {
		return sensor.getSensorId();
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

	public Instant getRecordedAt() {
		return recordedAt;
	}
}
