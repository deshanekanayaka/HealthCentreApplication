package com.westminster.healthcentre.model;

import jakarta.persistence.*;

/**
 * JPA entity for a Doctor. Discriminator value {@code "DOCTOR"} is written to
 * the {@code staff_type} column so Hibernate can reconstruct this subtype.
 *
 * <p>Doctor-specific columns are nullable at the database level because they
 * share the {@code staff_members} table with {@link Receptionist} rows
 * (SINGLE_TABLE inheritance).
 */
@Entity
@DiscriminatorValue("DOCTOR")
public class Doctor extends StaffMember {

    @Column(name = "licence_number", length = 12)
    private String licenceNumber;

    @Column(length = 100)
    private String specialisation;

    @Column(name = "consultations_per_week")
    private Integer consultationsPerWeek = 0;

    // ---- Getters & setters --------------------------------------------------

    public String getLicenceNumber()                        { return licenceNumber; }
    public void setLicenceNumber(String licenceNumber)      { this.licenceNumber = licenceNumber; }

    public String getSpecialisation()                       { return specialisation; }
    public void setSpecialisation(String specialisation)    { this.specialisation = specialisation; }

    public Integer getConsultationsPerWeek()                { return consultationsPerWeek; }
    public void setConsultationsPerWeek(Integer value)      { this.consultationsPerWeek = value; }
}
