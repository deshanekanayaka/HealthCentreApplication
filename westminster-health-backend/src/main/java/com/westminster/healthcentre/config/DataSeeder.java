package com.westminster.healthcentre.config;

import com.westminster.healthcentre.model.Doctor;
import com.westminster.healthcentre.model.Receptionist;
import com.westminster.healthcentre.model.StaffMember;
import com.westminster.healthcentre.repository.StaffMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with realistic sample data on first startup.
 *
 * <p>The {@code @Profile("!test")} annotation ensures this runner is skipped
 * during integration tests — each test manages its own data via {@code @BeforeEach}.
 *
 * <p>The guard {@code repository.count() == 0} means the seed runs only once:
 * on the very first deployment when the table is empty. Subsequent restarts
 * (e.g. Railway redeploys) leave existing data untouched.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StaffMemberRepository repository;

    public DataSeeder(StaffMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Database already contains data — skipping seed.");
            return;
        }

        log.info("Seeding database with sample staff...");
        repository.saveAll(sampleStaff());
        log.info("Seeded {} staff members.", repository.count());
    }

    private List<StaffMember> sampleStaff() {
        return List.of(
            doctor("DR0001", "Eleanor", "Barnes",
                   LocalDate.of(1978, 3, 12), "+44 7700 900101",
                   "GMC100001", "Cardiology", 24),

            doctor("DR0002", "James",   "Okafor",
                   LocalDate.of(1985, 7, 22), "+44 7700 900102",
                   "GMC100002", "Neurology", 18),

            doctor("DR0003", "Priya",   "Nair",
                   LocalDate.of(1990, 11, 5), "+44 7700 900103",
                   "GMC100003", "General Practice", 30),

            doctor("DR0004", "Michael", "Chen",
                   LocalDate.of(1982, 1, 30), "+44 7700 900104",
                   "GMC100004", "Orthopaedics", 15),

            doctor("DR0005", "Sofia",   "Andersson",
                   LocalDate.of(1995, 6, 18), null,
                   "GMC100005", "Paediatrics", 22),

            receptionist("RC0001", "Hannah",  "Clarke",
                         LocalDate.of(1992, 4, 14), "+44 7700 900201",
                         1, 37),

            receptionist("RC0002", "Daniel",  "Walsh",
                         LocalDate.of(1988, 9, 3),  "+44 7700 900202",
                         2, 37),

            receptionist("RC0003", "Amara",   "Diallo",
                         LocalDate.of(1997, 12, 20), null,
                         3, 20)
        );
    }

    // ---- Builders -----------------------------------------------------------

    private Doctor doctor(String staffId, String name, String surname,
                          LocalDate dob, String phone,
                          String licence, String specialisation, int consultations) {
        Doctor d = new Doctor();
        d.setStaffId(staffId);
        d.setName(name);
        d.setSurname(surname);
        d.setDob(dob);
        d.setPhoneNo(phone);
        d.setLicenceNumber(licence);
        d.setSpecialisation(specialisation);
        d.setConsultationsPerWeek(consultations);
        return d;
    }

    private Receptionist receptionist(String staffId, String name, String surname,
                                      LocalDate dob, String phone,
                                      int desk, int hours) {
        Receptionist r = new Receptionist();
        r.setStaffId(staffId);
        r.setName(name);
        r.setSurname(surname);
        r.setDob(dob);
        r.setPhoneNo(phone);
        r.setDeskNumber(desk);
        r.setHoursPerWeek(hours);
        return r;
    }
}
