package com.westminster.healthcentre.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTOs (Data Transfer Objects) for the Staff API.
 *
 * <p>Records are used throughout: they are immutable, concise, and Jackson
 * deserialises them automatically in Spring Boot 3. Bean Validation annotations
 * on record components are enforced by {@code @Valid} in the controller.
 *
 * <p>Entities never leave the service layer — only these DTOs cross the
 * Controller ↔ Service boundary.
 */
public final class StaffDtos {

    private StaffDtos() {} // utility class — not instantiated

    // =========================================================================
    // Request DTOs
    // =========================================================================

    /**
     * Request body for {@code POST /api/staff/doctors} and
     * {@code PUT /api/staff/doctors/{staffId}}.
     */
    public record CreateDoctorRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(max = 100, message = "Surname must not exceed 100 characters")
        String surname,

        @Past(message = "Date of birth must be in the past")
        LocalDate dob,

        @Pattern(
            regexp = "^[+\\d][\\d\\s\\-]{6,19}$",
            message = "Phone number format is invalid"
        )
        String phoneNo,

        @NotBlank(message = "Staff ID is required")
        @Pattern(
            regexp = "^[A-Z]{2}\\d{4}$",
            message = "Staff ID must be two uppercase letters followed by four digits (e.g. DR0001)"
        )
        String staffId,

        @Pattern(
            regexp = "^[A-Z0-9]{6,12}$",
            message = "Licence number must be 6–12 uppercase alphanumeric characters (e.g. GMC123456)"
        )
        String licenceNumber,

        @Size(max = 100, message = "Specialisation must not exceed 100 characters")
        String specialisation,

        @Min(value = 0,   message = "Consultations per week cannot be negative")
        @Max(value = 200, message = "Consultations per week cannot exceed 200")
        Integer consultationsPerWeek

    ) {}

    /**
     * Request body for {@code POST /api/staff/receptionists} and
     * {@code PUT /api/staff/receptionists/{staffId}}.
     */
    public record CreateReceptionistRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(max = 100, message = "Surname must not exceed 100 characters")
        String surname,

        @Past(message = "Date of birth must be in the past")
        LocalDate dob,

        @Pattern(
            regexp = "^[+\\d][\\d\\s\\-]{6,19}$",
            message = "Phone number format is invalid"
        )
        String phoneNo,

        @NotBlank(message = "Staff ID is required")
        @Pattern(
            regexp = "^[A-Z]{2}\\d{4}$",
            message = "Staff ID must be two uppercase letters followed by four digits (e.g. RC0001)"
        )
        String staffId,

        @Min(value = 1, message = "Desk number must be at least 1")
        Integer deskNumber,

        @Min(value = 1,  message = "Hours per week must be at least 1")
        @Max(value = 80, message = "Hours per week cannot exceed 80")
        Integer hoursPerWeek

    ) {}

    // =========================================================================
    // Response DTOs
    // =========================================================================

    /**
     * Unified response record for both Doctor and Receptionist.
     * Role-specific fields are {@code null} when not applicable.
     *
     * <p>Using one response type simplifies the frontend — it always gets the
     * same shape and checks the {@code type} field to decide what to display.
     */
    public record StaffMemberResponse(
        String  type,
        String  staffId,
        String  name,
        String  surname,
        LocalDate dob,
        String  phoneNo,
        // Doctor-specific (null for Receptionist)
        String  licenceNumber,
        String  specialisation,
        Integer consultationsPerWeek,
        // Receptionist-specific (null for Doctor)
        Integer deskNumber,
        Integer hoursPerWeek
    ) {}

    /** Summary statistics returned by {@code GET /api/staff/stats}. */
    public record StatsResponse(
        long total,
        long doctors,
        long receptionists,
        int  limit,
        long remainingCapacity
    ) {}

    /** Consistent error envelope returned by the global exception handler. */
    public record ErrorResponse(
        int    status,
        String message,
        String timestamp
    ) {}
}
