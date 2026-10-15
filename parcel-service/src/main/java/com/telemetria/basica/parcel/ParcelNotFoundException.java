package com.telemetria.basica.parcel;

public class ParcelNotFoundException extends RuntimeException {

	public ParcelNotFoundException(Long id) {
		super("No se encontró la parcela con ID " + id);
	}
}
