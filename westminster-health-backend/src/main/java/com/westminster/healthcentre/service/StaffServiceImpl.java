package com.westminster.healthcentre.service;

import com.westminster.healthcentre.dto.StaffDtos.*;
import com.westminster.healthcentre.exception.HealthCentreExceptions.*;
import com.westminster.healthcentre.model.Doctor;
import com.westminster.healthcentre.model.Receptionist;
import com.westminster.healthcentre.model.StaffMember;
import com.westminster.healthcentre.repository.StaffMemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Concrete implementation of {@link StaffService}.
 *
 * <h2>Responsibilities</h2>
 * <ol>
 *   <li>Enforce business rules (staff limit, duplicate ID, role mismatch).</li>
 *   <li>Map between JPA entities and DTOs — entities never leave this layer.</li>
 *   <li>Delegate persistence to {@link StaffMemberRepository}.</li>
 * </ol>
 *
 * <p>{@code @Transactional} on write methods ensures that if the save throws
 * a database exception, any partial changes are rolled back automatically.
 */
@Service
@Transactional(readOnly = true) // default: reads don't need a write transaction
public class StaffServiceImpl implements StaffService {

    private final StaffMemberRepository repository;

    /**
     * Injected from {@code application.properties} as {@code app.staff.limit}.
     * Defaults to 50 if not set.
     */
    @Value("${app.staff.limit:50}")
    private int staffLimit;

    // Constructor injection (preferred over @Autowired field injection)
    public StaffServiceImpl(StaffMemberRepository repository) {
        this.repository = repository;
    }

    // ---- Queries ------------------------------------------------------------

    @Override
    public List<StaffMemberResponse> getAllStaff(String search) {
        List<StaffMember> staff = (search == null || search.isBlank())
                ? repository.findAllByOrderBySurnameAscNameAsc()
                : repository.search(search.trim());

        return staff.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StaffMemberResponse getStaffById(String staffId) {
        return toResponse(findOrThrow(staffId));
    }

    @Override
    public StatsResponse getStats() {
        long doctors       = repository.countDoctors();
        long receptionists = repository.countReceptionists();
        long total         = doctors + receptionists;
        return new StatsResponse(total, doctors, receptionists, staffLimit, staffLimit - total);
    }

    // ---- Mutations ----------------------------------------------------------

    @Override
    @Transactional
    public StaffMemberResponse addDoctor(CreateDoctorRequest req) {
        checkCapacity();
        checkDuplicateId(req.staffId());

        Doctor doctor = new Doctor();
        applySharedFields(doctor, req.staffId(), req.name(), req.surname(), req.dob(), req.phoneNo());
        doctor.setLicenceNumber(req.licenceNumber());
        doctor.setSpecialisation(req.specialisation());
        doctor.setConsultationsPerWeek(req.consultationsPerWeek() != null ? req.consultationsPerWeek() : 0);

        return toResponse(repository.save(doctor));
    }

    @Override
    @Transactional
    public StaffMemberResponse addReceptionist(CreateReceptionistRequest req) {
        checkCapacity();
        checkDuplicateId(req.staffId());

        Receptionist receptionist = new Receptionist();
        applySharedFields(receptionist, req.staffId(), req.name(), req.surname(), req.dob(), req.phoneNo());
        receptionist.setDeskNumber(req.deskNumber());
        receptionist.setHoursPerWeek(req.hoursPerWeek());

        return toResponse(repository.save(receptionist));
    }

    @Override
    @Transactional
    public StaffMemberResponse updateDoctor(String staffId, CreateDoctorRequest req) {
        StaffMember existing = findOrThrow(staffId);
        if (!(existing instanceof Doctor doctor))
            throw new RoleMismatchException(staffId, "Doctor");

        applySharedFields(doctor, staffId, req.name(), req.surname(), req.dob(), req.phoneNo());
        doctor.setLicenceNumber(req.licenceNumber());
        doctor.setSpecialisation(req.specialisation());
        doctor.setConsultationsPerWeek(req.consultationsPerWeek() != null ? req.consultationsPerWeek() : 0);

        return toResponse(repository.save(doctor));
    }

    @Override
    @Transactional
    public StaffMemberResponse updateReceptionist(String staffId, CreateReceptionistRequest req) {
        StaffMember existing = findOrThrow(staffId);
        if (!(existing instanceof Receptionist receptionist))
            throw new RoleMismatchException(staffId, "Receptionist");

        applySharedFields(receptionist, staffId, req.name(), req.surname(), req.dob(), req.phoneNo());
        receptionist.setDeskNumber(req.deskNumber());
        receptionist.setHoursPerWeek(req.hoursPerWeek());

        return toResponse(repository.save(receptionist));
    }

    @Override
    @Transactional
    public void deleteStaff(String staffId) {
        StaffMember member = findOrThrow(staffId);
        repository.delete(member);
    }

    // ---- Private helpers ----------------------------------------------------

    private StaffMember findOrThrow(String staffId) {
        return repository.findByStaffId(staffId)
                .orElseThrow(() -> new StaffNotFoundException(staffId));
    }

    private void checkCapacity() {
        if (repository.count() >= staffLimit)
            throw new StaffLimitReachedException(staffLimit);
    }

    private void checkDuplicateId(String staffId) {
        if (repository.existsByStaffId(staffId))
            throw new DuplicateStaffIdException(staffId);
    }

    private void applySharedFields(StaffMember member, String staffId,
                                   String name, String surname,
                                   java.time.LocalDate dob, String phoneNo) {
        member.setStaffId(staffId);
        member.setName(name);
        member.setSurname(surname);
        member.setDob(dob);
        member.setPhoneNo(phoneNo);
    }

    /**
     * Maps any {@link StaffMember} subtype to the unified {@link StaffMemberResponse}.
     * Pattern matching (Java 16+) replaces instanceof casts cleanly.
     */
    private StaffMemberResponse toResponse(StaffMember s) {
        if (s instanceof Doctor d) {
            return new StaffMemberResponse(
                    "DOCTOR", d.getStaffId(), d.getName(), d.getSurname(),
                    d.getDob(), d.getPhoneNo(),
                    d.getLicenceNumber(), d.getSpecialisation(), d.getConsultationsPerWeek(),
                    null, null);
        }
        if (s instanceof Receptionist r) {
            return new StaffMemberResponse(
                    "RECEPTIONIST", r.getStaffId(), r.getName(), r.getSurname(),
                    r.getDob(), r.getPhoneNo(),
                    null, null, null,
                    r.getDeskNumber(), r.getHoursPerWeek());
        }
        throw new IllegalStateException("Unknown StaffMember subtype: " + s.getClass());
    }
}
