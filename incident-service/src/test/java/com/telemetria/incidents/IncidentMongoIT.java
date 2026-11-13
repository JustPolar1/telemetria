package com.telemetria.incidents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mongodb.MongoDBContainer;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentMongoIT {
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");
    @DynamicPropertySource static void mongo(DynamicPropertyRegistry properties) {
        MONGO.start();
        properties.add("spring.mongodb.uri", MONGO::getReplicaSetUrl);
    }
    @AfterAll static void stopMongo() { MONGO.stop(); }
    @Autowired MockMvc mvc;
    @Autowired IncidentRepository repository;

    @Test void crudPersistsInRealMongoAndUniqueIndexRejectsDuplicates() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/incidents'].post").exists());
        repository.deleteAll();
        String location = mvc.perform(post(IncidentControllerTests.BASE).contentType(MediaType.APPLICATION_JSON)
                .content(IncidentControllerTests.BODY)).andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertThat(repository.count()).isEqualTo(1);
        Incident saved = repository.findAll().getFirst();
        assertThatThrownBy(() -> repository.insert(new Incident(null, saved.folio(), saved.sensorId(),
                saved.titulo(), saved.descripcion(), saved.estado(), saved.fechaCreacion())))
                .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.sensorId").value("sensor-01"));
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content(IncidentControllerTests.BODY.replace("ABIERTA", "RESUELTA")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RESUELTA"));
        assertThat(repository.findById(saved.id()).orElseThrow().estado()).isEqualTo(IncidentStatus.RESUELTA);
        mvc.perform(get(IncidentControllerTests.BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        assertThat(repository.count()).isZero();
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }
}
