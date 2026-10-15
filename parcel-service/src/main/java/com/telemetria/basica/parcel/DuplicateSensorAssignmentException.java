package com.telemetria.basica.parcel;

public class DuplicateSensorAssignmentException extends RuntimeException {

	public DuplicateSensorAssignmentException(String sensorId) {
		super("El sensor " + sensorId + " ya está asignado a otra parcela");
	}
}
