package com.telemetria.incidents;

import java.util.List;
import org.springframework.data.domain.Page;

public record IncidentPage(List<Incident> content, long totalElements, int totalPages, int number, int size) {
    public static IncidentPage from(Page<Incident> page) {
        return new IncidentPage(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }
}
