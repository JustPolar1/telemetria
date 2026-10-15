package com.telemetria.basica.parcel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class ParcelControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ParcelRepository repository;

	@BeforeEach
	void clearParcels() {
		repository.deleteAll();
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
						  "ownerOrLead": "  Comunidad vecinal  "
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Huerto Norte"))
				.andExpect(jsonPath("$.location.latitude").value(28.6353))
				.andExpect(jsonPath("$.soilType").value("LIMOSO"))
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
						  "ownerOrLead": "Equipo de investigación"
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
				.andExpect(jsonPath("$.soilType").value("ARCILLOSO"));
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
						  "ownerOrLead": "Comunidad vecinal"
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
						  "ownerOrLead": "Comunidad vecinal"
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
						  "ownerOrLead": "Responsable inicial"
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
						  "ownerOrLead": "Nuevo responsable"
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
}
