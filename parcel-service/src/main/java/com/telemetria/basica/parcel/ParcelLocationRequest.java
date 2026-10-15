package com.telemetria.basica.parcel;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record ParcelLocationRequest(
		@NotNull(message = "La latitud es obligatoria")
		@DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
		@DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
		@Digits(integer = 3, fraction = 6, message = "La latitud admite hasta 6 decimales")
		BigDecimal latitude,
		@NotNull(message = "La longitud es obligatoria")
		@DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
		@DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
		@Digits(integer = 3, fraction = 6, message = "La longitud admite hasta 6 decimales")
		BigDecimal longitude) {
}
