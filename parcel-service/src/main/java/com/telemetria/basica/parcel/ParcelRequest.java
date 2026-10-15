package com.telemetria.basica.parcel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ParcelRequest(
		@NotBlank(message = "El nombre de la parcela es obligatorio")
		@Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
		@Pattern(regexp = "^[^\\p{Cc}<>]*$", message = "El nombre contiene caracteres o marcas no permitidos")
		String name,
		@NotNull(message = "La ubicación es obligatoria")
		@Valid
		ParcelLocationRequest location,
		@NotNull(message = "El tipo de suelo es obligatorio")
		SoilType soilType,
		@NotBlank(message = "El responsable es obligatorio")
		@Size(max = 150, message = "El responsable no puede exceder 150 caracteres")
		@Pattern(regexp = "^[^\\p{Cc}<>]*$", message = "El responsable contiene caracteres o marcas no permitidos")
		String ownerOrLead,
		@NotBlank(message = "El identificador del sensor es obligatorio")
		@Size(max = 100, message = "El identificador del sensor no puede exceder 100 caracteres")
		@Pattern(regexp = "^[A-Za-z0-9._:-]+$", message = "El identificador del sensor contiene caracteres no permitidos")
		String sensorId) {
}
