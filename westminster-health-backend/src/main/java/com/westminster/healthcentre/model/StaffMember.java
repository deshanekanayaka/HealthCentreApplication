package com.westminster.healthcentre.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity representing any staff member in the Westminster Health Centre.
 *
 * <h2>Inheritance strategy</h2>
 * <p>SINGLE_TABLE maps both {@link Doctor} and {@link Receptionist} to one
 * database table ({@code staff_members}). A {@code staff_type} discriminator
 * column tells Hibernate which subclass to instantiate on read. This avoids
 * joins and is appropriate at this scale (≤50 rows).
 *
 * <h2>Primary key</h2>
 * <p>An auto-generated {@code Long id} is used as the database PK.
 * The human-readable {@code staffId} (e.g. "DR0001") is a separate unique
 * column and is what the REST API exposes in URLs — keeping internal DB keys
 * out of the public contract.
 */
@Entity
@Table(name = "staff_members")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "staff_type", discriminatorType = DiscriminatorType.STRING)
public abstract class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable identifier, e.g. "DR0001". Unique across all staff. */
    @Column(name = "staff_id", unique = true, nullable = false, length = 6)
    private String staffId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Column(name = "phone_number", length = 20)
    private String phoneNo;

    // ---- Getters & setters --------------------------------------------------

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getStaffId()              { return staffId; }
    public void setStaffId(String staffId)  { this.staffId = staffId; }

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }

    public String getSurname()              { return surname; }
    public void setSurname(String surname)  { this.surname = surname; }

    public LocalDate getDob()               { return dob; }
    public void setDob(LocalDate dob)       { this.dob = dob; }

    public String getPhoneNo()              { return phoneNo; }
    public void setPhoneNo(String phoneNo)  { this.phoneNo = phoneNo; }
}
