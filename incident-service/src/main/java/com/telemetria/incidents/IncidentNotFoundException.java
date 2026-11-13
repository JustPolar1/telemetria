package com.telemetria.incidents;

public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(String id) { super("No existe la incidencia: " + id); }
}
