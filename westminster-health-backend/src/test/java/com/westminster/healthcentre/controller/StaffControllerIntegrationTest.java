package com.westminster.healthcentre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westminster.healthcentre.dto.StaffDtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.westminster.healthcentre.repository.StaffMemberRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link StaffController}.
 *
 * <p>{@code @SpringBootTest} starts the full application context (all beans,
 * JPA, validation, exception handler). {@code @AutoConfigureMockMvc} wires in
 * MockMvc so we can fire real HTTP requests without a running server.
 *
 * <p>The {@code test} profile swaps PostgreSQL for an H2 in-memory database
 * (see {@code src/test/resources/application-test.properties}), so these tests
 * run anywhere — locally and in CI — without any external dependencies.
 *
 * <p>Each test starts with a clean database via {@code @BeforeEach} so tests
 * are fully independent and can run in any order.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("StaffController — integration")
class StaffControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffMemberRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    // ---- POST /api/staff/doctors --------------------------------------------

    @Test
    @DisplayName("POST /api/staff/doctors — 201 with correct body")
    void addDoctor_returns201() throws Exception {
        CreateDoctorRequest req = new CreateDoctorRequest(
                "Alice", "Smith", null, null,
                "DR0001", "GMC123456", "Cardiology", 20);

        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("DR0001")))
                .andExpect(jsonPath("$.staffId").value("DR0001"))
                .andExpect(jsonPath("$.type").value("DOCTOR"))
                .andExpect(jsonPath("$.specialisation").value("Cardiology"));
    }

    @Test
    @DisplayName("POST /api/staff/doctors — 400 when name is blank")
    void addDoctor_blankName_returns400() throws Exception {
        CreateDoctorRequest req = new CreateDoctorRequest(
                "", "Smith", null, null,
                "DR0001", null, null, 0);

        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("name")));
    }

    @Test
    @DisplayName("POST /api/staff/doctors — 400 when staff ID format is wrong")
    void addDoctor_invalidStaffId_returns400() throws Exception {
        CreateDoctorRequest req = new CreateDoctorRequest(
                "Alice", "Smith", null, null,
                "bad-id", null, null, 0);

        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/staff/doctors — 409 when staff ID already exists")
    void addDoctor_duplicateId_returns409() throws Exception {
        CreateDoctorRequest req = new CreateDoctorRequest(
                "Alice", "Smith", null, null,
                "DR0001", null, null, 0);

        // First insert
        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("DR0001")));
    }

    @Test
    @DisplayName("POST /api/staff/doctors — 409 when staff limit is reached")
    void addDoctor_atCapacity_returns409() throws Exception {
        // Limit is 5 in test profile — fill it up
        for (int i = 1; i <= 5; i++) {
            CreateDoctorRequest req = new CreateDoctorRequest(
                    "Name" + i, "Sur" + i, null, null,
                    "DR000" + i, null, null, 0);
            mockMvc.perform(post("/api/staff/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        CreateDoctorRequest overflow = new CreateDoctorRequest(
                "Extra", "Person", null, null, "DR0099", null, null, 0);

        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overflow)))
                .andExpect(status().isConflict());
    }

    // ---- POST /api/staff/receptionists --------------------------------------

    @Test
    @DisplayName("POST /api/staff/receptionists — 201 with correct body")
    void addReceptionist_returns201() throws Exception {
        CreateReceptionistRequest req = new CreateReceptionistRequest(
                "Jane", "Doe", null, null,
                "RC0001", 3, 37);

        mockMvc.perform(post("/api/staff/receptionists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("RECEPTIONIST"))
                .andExpect(jsonPath("$.deskNumber").value(3))
                .andExpect(jsonPath("$.hoursPerWeek").value(37));
    }

    // ---- GET /api/staff -----------------------------------------------------

    @Test
    @DisplayName("GET /api/staff — returns all staff sorted by surname")
    void getAllStaff_returnsSortedList() throws Exception {
        addDoctorDirectly("DR0001", "Zara", "Young");
        addDoctorDirectly("DR0002", "Alice", "Smith");

        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].surname").value("Smith"))
                .andExpect(jsonPath("$[1].surname").value("Young"));
    }

    @Test
    @DisplayName("GET /api/staff?search= — filters by partial name")
    void getAllStaff_withSearch_filtersResults() throws Exception {
        addDoctorDirectly("DR0001", "Alice", "Smith");
        addDoctorDirectly("DR0002", "Bob",   "Taylor");

        mockMvc.perform(get("/api/staff").param("search", "ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/staff — returns empty list when no staff registered")
    void getAllStaff_empty_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---- GET /api/staff/{staffId} -------------------------------------------

    @Test
    @DisplayName("GET /api/staff/{staffId} — 200 when found")
    void getById_found_returns200() throws Exception {
        addDoctorDirectly("DR0001", "Alice", "Smith");

        mockMvc.perform(get("/api/staff/DR0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffId").value("DR0001"));
    }

    @Test
    @DisplayName("GET /api/staff/{staffId} — 404 when not found")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/staff/XX9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("XX9999")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ---- GET /api/staff/stats -----------------------------------------------

    @Test
    @DisplayName("GET /api/staff/stats — returns correct counts")
    void getStats_returnsCorrectCounts() throws Exception {
        addDoctorDirectly("DR0001", "Alice", "Smith");
        addReceptionistDirectly("RC0001", "Jane", "Doe");

        mockMvc.perform(get("/api/staff/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.doctors").value(1))
                .andExpect(jsonPath("$.receptionists").value(1))
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.remainingCapacity").value(3));
    }

    // ---- PUT /api/staff/doctors/{staffId} -----------------------------------

    @Test
    @DisplayName("PUT /api/staff/doctors/{staffId} — 200 with updated fields")
    void updateDoctor_returns200() throws Exception {
        addDoctorDirectly("DR0001", "Alice", "Smith");

        CreateDoctorRequest update = new CreateDoctorRequest(
                "Alice", "Johnson", null, null,
                "DR0001", "GMC999999", "Neurology", 15);

        mockMvc.perform(put("/api/staff/doctors/DR0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Johnson"))
                .andExpect(jsonPath("$.specialisation").value("Neurology"));
    }

    @Test
    @DisplayName("PUT /api/staff/doctors/{staffId} — 400 when ID belongs to a Receptionist")
    void updateDoctor_roleMismatch_returns400() throws Exception {
        addReceptionistDirectly("RC0001", "Jane", "Doe");

        CreateDoctorRequest req = new CreateDoctorRequest(
                "Jane", "Doe", null, null, "RC0001", null, null, 0);

        mockMvc.perform(put("/api/staff/doctors/RC0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /api/staff/{staffId} ----------------------------------------

    @Test
    @DisplayName("DELETE /api/staff/{staffId} — 204 on success")
    void deleteStaff_returns204() throws Exception {
        addDoctorDirectly("DR0001", "Alice", "Smith");

        mockMvc.perform(delete("/api/staff/DR0001"))
                .andExpect(status().isNoContent());

        // Confirm it's gone
        mockMvc.perform(get("/api/staff/DR0001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/staff/{staffId} — 404 when not found")
    void deleteStaff_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/staff/XX9999"))
                .andExpect(status().isNotFound());
    }

    // ---- Helpers ------------------------------------------------------------

    private void addDoctorDirectly(String staffId, String name, String surname) throws Exception {
        CreateDoctorRequest req = new CreateDoctorRequest(
                name, surname, null, null, staffId, null, null, 0);
        mockMvc.perform(post("/api/staff/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    private void addReceptionistDirectly(String staffId, String name, String surname) throws Exception {
        CreateReceptionistRequest req = new CreateReceptionistRequest(
                name, surname, null, null, staffId, 1, 37);
        mockMvc.perform(post("/api/staff/receptionists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
