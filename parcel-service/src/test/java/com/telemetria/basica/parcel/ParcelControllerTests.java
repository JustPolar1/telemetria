package com.telemetria.basica.parcel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class ParcelControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ParcelRepository repository;

	@MockitoBean
	private TelemetryClient telemetryClient;

	@BeforeEach
	void clearParcels() {
		repository.deleteAll();
		when(telemetryClient.findLatestReadings(org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(List.of());
	}

	@Test
	void createsParcelAndReturnsItsDatabaseId() throws Exception {
		mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "  Huerto Norte  ",
						  "location": {
						    "latitude": 28.6353,
						    "longitude": -106.0889
						  },
						  "soilType": "LIMOSO",
						  "ownerOrLead": "  Comunidad vecinal  ",
						  "sensorId": "sensor-01"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Huerto Norte"))
				.andExpect(jsonPath("$.location.latitude").value(28.6353))
				.andExpect(jsonPath("$.soilType").value("LIMOSO"))
				.andExpect(jsonPath("$.sensorId").value("sensor-01"))
				.andExpect(jsonPath("$.ownerOrLead").value("Comunidad vecinal"))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void listsParcelsAndRetrievesOneById() throws Exception {
		String location = mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Huerto Sur",
						  "location": {
						    "latitude": 19.4326,
						    "longitude": -99.1332
						  },
						  "soilType": "ARCILLOSO",
						  "ownerOrLead": "Equipo de investigación",
						  "sensorId": "sensor-02"
						}
						"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");
		Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

		mockMvc.perform(get("/api/v1/parcels"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Huerto Sur"));

		mockMvc.perform(get("/api/v1/parcels/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.soilType").value("ARCILLOSO"))
				.andExpect(jsonPath("$.sensorId").value("sensor-02"))
				.andExpect(jsonPath("$.telemetryStatus").value("NO_DATA"))
				.andExpect(jsonPath("$.latestReadings").isEmpty());
	}

	@Test
	void rejectsCoordinatesOutsideGpsRange() throws Exception {
		mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Huerto inválido",
						  "location": {
						    "latitude": 91,
						    "longitude": -106
						  },
						  "soilType": "ARENOSO",
						  "ownerOrLead": "Comunidad vecinal",
						  "sensorId": "sensor-03"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("latitude")));
	}

	@Test
	void rejectsMarkupInTextFields() throws Exception {
		mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "<script>alert(1)</script>",
						  "location": {
						    "latitude": 28,
						    "longitude": -106
						  },
						  "soilType": "ARENOSO",
						  "ownerOrLead": "Comunidad vecinal",
						  "sensorId": "sensor-04"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(
						org.hamcrest.Matchers.containsString("name")));
	}

	@Test
	void returnsNotFoundForAnUnknownParcel() throws Exception {
		mockMvc.perform(get("/api/v1/parcels/987654"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("No se encontró la parcela con ID 987654"));
	}

	@Test
	void updatesAndDeletesAParcel() throws Exception {
		String location = mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Huerto inicial",
						  "location": {
						    "latitude": 28.6,
						    "longitude": -106.1
						  },
						  "soilType": "ARENOSO",
						  "ownerOrLead": "Responsable inicial",
						  "sensorId": "sensor-05"
						}
						"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");
		String id = location.substring(location.lastIndexOf('/') + 1);

		mockMvc.perform(put("/api/v1/parcels/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Huerto actualizado",
						  "location": {
						    "latitude": 19.4,
						    "longitude": -99.1
						  },
						  "soilType": "LIMOSO",
						  "ownerOrLead": "Nuevo responsable",
						  "sensorId": "sensor-05"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Huerto actualizado"))
				.andExpect(jsonPath("$.soilType").value("LIMOSO"));

		mockMvc.perform(delete("/api/v1/parcels/{id}", id))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/v1/parcels/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void includesLatestSensorReadingsInParcelDetails() throws Exception {
		when(telemetryClient.findLatestReadings("sensor-06"))
				.thenReturn(List.of(new TelemetryReadingSnapshot(
						17L,
						"sensor-06",
						"PH",
						new BigDecimal("6.5"),
						Instant.parse("2026-10-02T18:30:00Z"),
						Instant.parse("2026-10-02T18:31:00Z"))));

		String location = mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Huerto con datos",
						  "location": {
						    "latitude": 28,
						    "longitude": -106
						  },
						  "soilType": "LIMOSO",
						  "ownerOrLead": "Comunidad",
						  "sensorId": "sensor-06"
						}
						"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");
		String id = location.substring(location.lastIndexOf('/') + 1);

		mockMvc.perform(get("/api/v1/parcels/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.telemetryStatus").value("AVAILABLE"))
				.andExpect(jsonPath("$.latestReadings.length()").value(1))
				.andExpect(jsonPath("$.latestReadings[0].metric").value("PH"))
				.andExpect(jsonPath("$.latestReadings[0].value").value(6.5));
	}

	@Test
	void returnsServiceUnavailableWhenTelemetryCannotBeReached() throws Exception {
		when(telemetryClient.findLatestReadings("sensor-07"))
				.thenThrow(new TelemetryServiceUnavailableException(new IllegalStateException("offline")));

		String location = mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Huerto sin conexión",
						  "location": {
						    "latitude": 28,
						    "longitude": -106
						  },
						  "soilType": "LIMOSO",
						  "ownerOrLead": "Comunidad",
						  "sensorId": "sensor-07"
						}
						"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");
		String id = location.substring(location.lastIndexOf('/') + 1);

		mockMvc.perform(get("/api/v1/parcels/{id}", id))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.error").value(
						org.hamcrest.Matchers.containsString("servicio de telemetría")));
	}

	@Test
	void rejectsAssigningOneSensorToMoreThanOneParcel() throws Exception {
		String body = """
				{
				  "name": "Huerto duplicado",
				  "location": {
				    "latitude": 28,
				    "longitude": -106
				  },
				  "soilType": "LIMOSO",
				  "ownerOrLead": "Comunidad",
				  "sensorId": "sensor-08"
				}
				""";
		mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/parcels")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value(
						org.hamcrest.Matchers.containsString("ya está asignado")));
	}
}
