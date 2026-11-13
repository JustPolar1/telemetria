package com.telemetria.incidents;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
class IncidentControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean IncidentService service;
    static final String BASE = "/api/v1/incidents";
    static final String BODY = """
        {"sensorId":"sensor-01","titulo":"Fallo","descripcion":"Sin lecturas","estado":"ABIERTA"}
        """;
    private final Incident incident = new Incident("id", "folio", "sensor-01", "Fallo", "Sin lecturas", IncidentStatus.ABIERTA, Instant.parse("2026-11-11T00:00:00Z"));

    @Test void exposesCrudAndSerializesResponses() throws Exception {
        when(service.create(any())).thenReturn(incident);
        when(service.find("id")).thenReturn(incident);
        when(service.update(eq("id"), any())).thenReturn(incident);
        when(service.list(0, 20)).thenReturn(new PageImpl<>(List.of(incident)));
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("Location", BASE + "/id"))
                .andExpect(jsonPath("$.sensorId").value("sensor-01"));
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value("id"));
        mvc.perform(get(BASE + "/id")).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ABIERTA"));
        mvc.perform(put(BASE + "/id").contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isOk());
        mvc.perform(delete(BASE + "/id")).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).delete("id");
    }

    @Test void validatesRequiredFields() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test void rejectsMalformedJsonUnknownStatusAndInvalidPagination() throws Exception {
        for (String body : List.of("{", BODY.replace("ABIERTA", "INVALIDA"))) {
            mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        for (String query : List.of("?page=-1", "?size=0", "?size=101", "?page=abc")) {
            mvc.perform(get(BASE + query)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test void returnsBusinessErrorsAndHidesDatabaseDetails() throws Exception {
        when(service.find("missing")).thenThrow(new IncidentNotFoundException("missing"));
        mvc.perform(get(BASE + "/missing")).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").exists());
        when(service.create(any())).thenThrow(new DuplicateIncidentException());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isConflict());
        when(service.find("id")).thenThrow(new DataAccessResourceFailureException("secret connection string"));
        mvc.perform(get(BASE + "/id")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("No se pudo acceder a la base de datos; intenta más tarde"));
    }
}
