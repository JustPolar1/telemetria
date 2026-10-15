package com.telemetria.basica.parcel;

public class TelemetryServiceUnavailableException extends RuntimeException {

	public TelemetryServiceUnavailableException(Throwable cause) {
		super("El servicio de telemetría no está disponible; intenta nuevamente", cause);
	}
}
