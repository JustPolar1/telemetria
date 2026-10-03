package com.telemetria.basica.telemetry;

import java.util.Locale;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SensorMetricConverter implements AttributeConverter<SensorMetric, String> {

	@Override
	public String convertToDatabaseColumn(SensorMetric metric) {
		return metric == null ? null : metric.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public SensorMetric convertToEntityAttribute(String value) {
		return value == null ? null : SensorMetric.valueOf(value.toUpperCase(Locale.ROOT));
	}
}
