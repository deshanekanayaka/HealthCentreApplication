package com.westminster.healthcentre.controller;

import com.westminster.healthcentre.dto.StaffDtos.*;
import com.westminster.healthcentre.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for the {@code /api/staff} resource.
 *
 * <p>The controller's only responsibilities are:
 * <ol>
 *   <li>Declare HTTP mappings and status codes.</li>
 *   <li>Delegate to {@link StaffService} (no business logic here).</li>
 *   <li>Build the {@link ResponseEntity} with the correct status and headers.</li>
 * </ol>
 *
 * <p>Error responses are handled centrally by {@code GlobalExceptionHandler},
 * so no try/catch blocks appear here.
 */
@RestController
@RequestMapping("/api/staff")
@Tag(name = "Staff", description = "Manage doctors and receptionists at the health centre")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // ---- Queries ------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "List all staff",
        description = "Returns all staff sorted by surname. Pass ?search= to filter by name, surname, or staff ID."
    )
    public ResponseEntity<List<StaffMemberResponse>> getAllStaff(
            @Parameter(description = "Optional partial-match filter")
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(staffService.getAllStaff(search));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get staff statistics", description = "Returns total counts and remaining capacity")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(staffService.getStats());
    }

    @GetMapping("/{staffId}")
    @Operation(summary = "Get a single staff member by staff ID")
    @ApiResponse(responseCode = "404", description = "Staff member not found")
    public ResponseEntity<StaffMemberResponse> getById(
            @Parameter(description = "Staff ID, e.g. DR0001")
            @PathVariable String staffId) {
        return ResponseEntity.ok(staffService.getStaffById(staffId));
    }

    // ---- Doctor mutations ---------------------------------------------------

    @PostMapping("/doctors")
    @Operation(summary = "Add a new doctor")
    @ApiResponse(responseCode = "201", description = "Doctor created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "409", description = "Duplicate staff ID or staff limit reached")
    public ResponseEntity<StaffMemberResponse> addDoctor(
            @Valid @RequestBody CreateDoctorRequest request) {
        StaffMemberResponse response = staffService.addDoctor(request);
        URI location = URI.create("/api/staff/" + response.staffId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/doctors/{staffId}")
    @Operation(summary = "Update an existing doctor")
    @ApiResponse(responseCode = "400", description = "Staff ID belongs to a Receptionist")
    @ApiResponse(responseCode = "404", description = "Staff member not found")
    public ResponseEntity<StaffMemberResponse> updateDoctor(
            @PathVariable String staffId,
            @Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.ok(staffService.updateDoctor(staffId, request));
    }

    // ---- Receptionist mutations ----------------------------------------------

    @PostMapping("/receptionists")
    @Operation(summary = "Add a new receptionist")
    @ApiResponse(responseCode = "201", description = "Receptionist created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "409", description = "Duplicate staff ID or staff limit reached")
    public ResponseEntity<StaffMemberResponse> addReceptionist(
            @Valid @RequestBody CreateReceptionistRequest request) {
        StaffMemberResponse response = staffService.addReceptionist(request);
        URI location = URI.create("/api/staff/" + response.staffId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/receptionists/{staffId}")
    @Operation(summary = "Update an existing receptionist")
    @ApiResponse(responseCode = "400", description = "Staff ID belongs to a Doctor")
    @ApiResponse(responseCode = "404", description = "Staff member not found")
    public ResponseEntity<StaffMemberResponse> updateReceptionist(
            @PathVariable String staffId,
            @Valid @RequestBody CreateReceptionistRequest request) {
        return ResponseEntity.ok(staffService.updateReceptionist(staffId, request));
    }

    // ---- Delete -------------------------------------------------------------

    @DeleteMapping("/{staffId}")
    @Operation(summary = "Remove a staff member")
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @ApiResponse(responseCode = "404", description = "Staff member not found")
    public ResponseEntity<Void> deleteStaff(@PathVariable String staffId) {
        staffService.deleteStaff(staffId);
        return ResponseEntity.noContent().build();
    }
}
