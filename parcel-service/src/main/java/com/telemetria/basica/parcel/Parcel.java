package com.telemetria.basica.parcel;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parcels")
public class Parcel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	@Convert(converter = SoilTypeConverter.class)
	@Column(name = "soil_type", nullable = false, length = 20)
	private SoilType soilType;

	@Column(name = "owner_or_lead", nullable = false, length = 150)
	private String ownerOrLead;

	@Generated(event = EventType.INSERT)
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	protected Parcel() {
	}

	public Parcel(
			String name,
			BigDecimal latitude,
			BigDecimal longitude,
			SoilType soilType,
			String ownerOrLead) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.soilType = soilType;
		this.ownerOrLead = ownerOrLead;
	}

	public void update(
			String name,
			BigDecimal latitude,
			BigDecimal longitude,
			SoilType soilType,
			String ownerOrLead) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.soilType = soilType;
		this.ownerOrLead = ownerOrLead;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public SoilType getSoilType() {
		return soilType;
	}

	public String getOwnerOrLead() {
		return ownerOrLead;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
