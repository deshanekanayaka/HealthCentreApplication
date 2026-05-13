package com.westminster.healthcentre.repository;

import com.westminster.healthcentre.model.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link StaffMember} and its subtypes.
 *
 * <p>Spring generates all SQL at startup from method names and {@code @Query}
 * annotations — no boilerplate implementation needed. The inherited
 * {@code JpaRepository} methods ({@code save}, {@code findAll},
 * {@code deleteById}, etc.) work polymorphically across Doctor and Receptionist.
 */
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    /** Exact match on the human-readable staff ID (e.g. "DR0001"). */
    Optional<StaffMember> findByStaffId(String staffId);

    /** Used for duplicate-ID checks before insert. */
    boolean existsByStaffId(String staffId);

    /**
     * Returns all staff sorted alphabetically by surname then name.
     * Spring derives the query entirely from the method name.
     */
    List<StaffMember> findAllByOrderBySurnameAscNameAsc();

    /**
     * Case-insensitive partial match across name, surname, and staff ID.
     * JPQL (not native SQL) keeps this database-agnostic.
     */
    @Query("""
           SELECT s FROM StaffMember s
           WHERE LOWER(s.name)    LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(s.surname) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(s.staffId) LIKE LOWER(CONCAT('%', :q, '%'))
           ORDER BY s.surname ASC, s.name ASC
           """)
    List<StaffMember> search(@Param("q") String query);

    /** Counts only Doctor rows via the SINGLE_TABLE discriminator. */
    @Query("SELECT COUNT(d) FROM Doctor d")
    long countDoctors();

    /** Counts only Receptionist rows via the SINGLE_TABLE discriminator. */
    @Query("SELECT COUNT(r) FROM Receptionist r")
    long countReceptionists();
}
