package com.westminster.healthcentre.service;

import com.westminster.healthcentre.dto.StaffDtos.*;
import com.westminster.healthcentre.exception.HealthCentreExceptions.*;
import com.westminster.healthcentre.model.Doctor;
import com.westminster.healthcentre.model.Receptionist;
import com.westminster.healthcentre.repository.StaffMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StaffServiceImpl}.
 *
 * <p>{@code @ExtendWith(MockitoExtension.class)} initialises mocks without
 * starting a Spring application context — these tests run in milliseconds.
 * The repository is mocked so no database is involved.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StaffServiceImpl")
class StaffServiceImplTest {

    @Mock
    private StaffMemberRepository repository;

    @InjectMocks
    private StaffServiceImpl service;

    @BeforeEach
    void injectStaffLimit() {
        // Simulate @Value("${app.staff.limit:50}") = 3 for capacity tests
        ReflectionTestUtils.setField(service, "staffLimit", 3);
    }

    // ---- getAllStaff --------------------------------------------------------

    @Test
    @DisplayName("getAllStaff returns mapped responses sorted by surname")
    void getAllStaff_returnsAll() {
        Doctor doctor = doctor("DR0001", "Alice", "Smith");
        when(repository.findAllByOrderBySurnameAscNameAsc()).thenReturn(List.of(doctor));

        List<StaffMemberResponse> result = service.getAllStaff(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).staffId()).isEqualTo("DR0001");
        assertThat(result.get(0).type()).isEqualTo("DOCTOR");
    }

    @Test
    @DisplayName("getAllStaff with search term delegates to repository.search()")
    void getAllStaff_withSearch_delegatesToSearch() {
        when(repository.search("alice")).thenReturn(List.of());
        service.getAllStaff("alice");
        verify(repository).search("alice");
        verify(repository, never()).findAllByOrderBySurnameAscNameAsc();
    }

    // ---- getStaffById -------------------------------------------------------

    @Test
    @DisplayName("getStaffById returns response when found")
    void getStaffById_found() {
        when(repository.findByStaffId("DR0001")).thenReturn(Optional.of(doctor("DR0001", "Alice", "Smith")));

        StaffMemberResponse response = service.getStaffById("DR0001");

        assertThat(response.staffId()).isEqualTo("DR0001");
        assertThat(response.name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("getStaffById throws StaffNotFoundException when not found")
    void getStaffById_notFound_throws() {
        when(repository.findByStaffId("XX9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStaffById("XX9999"))
                .isInstanceOf(StaffNotFoundException.class)
                .hasMessageContaining("XX9999");
    }

    // ---- addDoctor ----------------------------------------------------------

    @Test
    @DisplayName("addDoctor saves and returns the new doctor")
    void addDoctor_success() {
        when(repository.count()).thenReturn(0L);
        when(repository.existsByStaffId("DR0001")).thenReturn(false);
        when(repository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateDoctorRequest req = new CreateDoctorRequest(
                "Alice", "Smith", null, null,
                "DR0001", "GMC123456", "Cardiology", 20);

        StaffMemberResponse response = service.addDoctor(req);

        assertThat(response.staffId()).isEqualTo("DR0001");
        assertThat(response.specialisation()).isEqualTo("Cardiology");
        assertThat(response.consultationsPerWeek()).isEqualTo(20);
        verify(repository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("addDoctor throws DuplicateStaffIdException when ID already exists")
    void addDoctor_duplicateId_throws() {
        when(repository.count()).thenReturn(1L);
        when(repository.existsByStaffId("DR0001")).thenReturn(true);

        CreateDoctorRequest req = new CreateDoctorRequest(
                "Bob", "Jones", null, null, "DR0001", null, null, 0);

        assertThatThrownBy(() -> service.addDoctor(req))
                .isInstanceOf(DuplicateStaffIdException.class)
                .hasMessageContaining("DR0001");
    }

    @Test
    @DisplayName("addDoctor throws StaffLimitReachedException when at capacity")
    void addDoctor_atCapacity_throws() {
        when(repository.count()).thenReturn(3L); // limit is 3

        CreateDoctorRequest req = new CreateDoctorRequest(
                "Carol", "White", null, null, "DR0002", null, null, 0);

        assertThatThrownBy(() -> service.addDoctor(req))
                .isInstanceOf(StaffLimitReachedException.class);
        verify(repository, never()).save(any());
    }

    // ---- updateDoctor -------------------------------------------------------

    @Test
    @DisplayName("updateDoctor throws RoleMismatchException when staffId belongs to a Receptionist")
    void updateDoctor_roleMismatch_throws() {
        Receptionist r = receptionist("RC0001", "Jane", "Doe");
        when(repository.findByStaffId("RC0001")).thenReturn(Optional.of(r));

        CreateDoctorRequest req = new CreateDoctorRequest(
                "Jane", "Doe", null, null, "RC0001", null, null, 0);

        assertThatThrownBy(() -> service.updateDoctor("RC0001", req))
                .isInstanceOf(RoleMismatchException.class)
                .hasMessageContaining("RC0001");
    }

    // ---- deleteStaff --------------------------------------------------------

    @Test
    @DisplayName("deleteStaff calls repository.delete() for an existing member")
    void deleteStaff_success() {
        Doctor doctor = doctor("DR0001", "Alice", "Smith");
        when(repository.findByStaffId("DR0001")).thenReturn(Optional.of(doctor));

        service.deleteStaff("DR0001");

        verify(repository).delete(doctor);
    }

    @Test
    @DisplayName("deleteStaff throws StaffNotFoundException for unknown ID")
    void deleteStaff_notFound_throws() {
        when(repository.findByStaffId("XX9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteStaff("XX9999"))
                .isInstanceOf(StaffNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    // ---- getStats -----------------------------------------------------------

    @Test
    @DisplayName("getStats returns correct counts and remaining capacity")
    void getStats_returnsCorrectCounts() {
        when(repository.count()).thenReturn(2L);
        when(repository.countDoctors()).thenReturn(1L);
        when(repository.countReceptionists()).thenReturn(1L);

        StatsResponse stats = service.getStats();

        assertThat(stats.total()).isEqualTo(2);
        assertThat(stats.doctors()).isEqualTo(1);
        assertThat(stats.receptionists()).isEqualTo(1);
        assertThat(stats.limit()).isEqualTo(3);
        assertThat(stats.remainingCapacity()).isEqualTo(1);
    }

    // ---- Test fixtures ------------------------------------------------------

    private Doctor doctor(String staffId, String name, String surname) {
        Doctor d = new Doctor();
        d.setStaffId(staffId);
        d.setName(name);
        d.setSurname(surname);
        d.setConsultationsPerWeek(0);
        return d;
    }

    private Receptionist receptionist(String staffId, String name, String surname) {
        Receptionist r = new Receptionist();
        r.setStaffId(staffId);
        r.setName(name);
        r.setSurname(surname);
        return r;
    }
}
