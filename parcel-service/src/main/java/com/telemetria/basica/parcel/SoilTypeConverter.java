package com.telemetria.basica.parcel;

import java.util.Locale;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SoilTypeConverter implements AttributeConverter<SoilType, String> {

	@Override
	public String convertToDatabaseColumn(SoilType soilType) {
		return soilType == null ? null : soilType.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public SoilType convertToEntityAttribute(String value) {
		return value == null ? null : SoilType.valueOf(value.toUpperCase(Locale.ROOT));
	}
}
