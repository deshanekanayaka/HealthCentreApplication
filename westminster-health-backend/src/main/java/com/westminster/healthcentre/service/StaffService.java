package com.westminster.healthcentre.service;

import com.westminster.healthcentre.dto.StaffDtos.*;

import java.util.List;

/**
 * Service contract for staff management operations.
 *
 * <p>Defining the service as an interface allows the controller to depend on
 * the abstraction rather than the concrete implementation. This makes unit
 * testing straightforward — Mockito can stub the interface without needing
 * Spring context or a database.
 */
public interface StaffService {

    /**
     * Returns all staff, sorted by surname then name.
     * If {@code search} is non-blank, filters by name, surname, or staff ID
     * (case-insensitive partial match).
     */
    List<StaffMemberResponse> getAllStaff(String search);

    /** Returns the staff member with the given staffId, or throws 404. */
    StaffMemberResponse getStaffById(String staffId);

    /** Adds a new doctor. Throws 409 if the ID already exists or limit is reached. */
    StaffMemberResponse addDoctor(CreateDoctorRequest request);

    /** Adds a new receptionist. Throws 409 if the ID already exists or limit is reached. */
    StaffMemberResponse addReceptionist(CreateReceptionistRequest request);

    /**
     * Replaces an existing doctor's fields. The staffId in the path must match
     * a Doctor — passing a Receptionist's ID throws 400.
     */
    StaffMemberResponse updateDoctor(String staffId, CreateDoctorRequest request);

    /**
     * Replaces an existing receptionist's fields. The staffId in the path must
     * match a Receptionist — passing a Doctor's ID throws 400.
     */
    StaffMemberResponse updateReceptionist(String staffId, CreateReceptionistRequest request);

    /** Removes the staff member. Throws 404 if the ID is not found. */
    void deleteStaff(String staffId);

    /** Returns a snapshot of current staff counts and remaining capacity. */
    StatsResponse getStats();
}
