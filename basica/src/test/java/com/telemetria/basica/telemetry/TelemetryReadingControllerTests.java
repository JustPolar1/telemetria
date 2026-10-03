package com.telemetria.basica.telemetry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TelemetryReadingControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TelemetryReadingRepository repository;

	@BeforeEach
	void clearReadings() {
		repository.deleteAll();
	}

	@Test
	void receivesAndStoresAReading() throws Exception {
		mockMvc.perform(post("/api/lecturas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "sensorId": "sensor-01",
						  "metric": "TEMPERATURE",
						  "value": 22.75,
						  "measuredAt": "2026-10-02T18:30:00Z"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.sensorId").value("sensor-01"))
				.andExpect(jsonPath("$.receivedAt").exists());
	}

	@Test
	void rejectsAReadingWithoutRequiredFields() throws Exception {
		mockMvc.perform(post("/api/lecturas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "sensorId": "sensor-01",
						  "value": 22.75
						}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listsReadingsForSensorWithDateFiltersAndPagination() throws Exception {
		mockMvc.perform(post("/api/lecturas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "sensorId": "sensor-01",
						  "metric": "TEMPERATURE",
						  "value": 20.00,
						  "measuredAt": "2026-10-01T10:00:00Z"
						}
						"""))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/lecturas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "sensorId": "sensor-01",
						  "metric": "HUMIDITY",
						  "value": 45.5,
						  "measuredAt": "2026-10-02T10:00:00Z"
						}
						"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/sensores/sensor-01/lecturas")
				.param("from", "2026-10-02T00:00:00Z")
				.param("to", "2026-10-03T00:00:00Z")
				.param("page", "0")
				.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].metric").value("HUMIDITY"));
	}

	@Test
	void rejectsAnInvalidDateRange() throws Exception {
		mockMvc.perform(get("/api/sensores/sensor-01/lecturas")
				.param("from", "2026-10-03T00:00:00Z")
				.param("to", "2026-10-02T00:00:00Z"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}
}
