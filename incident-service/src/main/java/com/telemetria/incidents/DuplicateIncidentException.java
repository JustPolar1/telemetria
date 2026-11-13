package com.telemetria.incidents;

public class DuplicateIncidentException extends RuntimeException {
    public DuplicateIncidentException() { super("Ya existe una incidencia con ese folio"); }
}
