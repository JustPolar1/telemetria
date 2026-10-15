package com.telemetria.basica.telemetry;

public class InvalidMeasurementException extends RuntimeException {

	public InvalidMeasurementException(SensorMetric metric) {
		super("El valor está fuera del rango permitido para la medición " + metric);
	}
}
