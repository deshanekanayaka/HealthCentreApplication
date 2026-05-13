package com.westminster.healthcentre.model;

import jakarta.persistence.*;

/**
 * JPA entity for a Receptionist. Discriminator value {@code "RECEPTIONIST"}.
 *
 * <p>Receptionist-specific columns are nullable at the database level because
 * they share the {@code staff_members} table with {@link Doctor} rows
 * (SINGLE_TABLE inheritance).
 */
@Entity
@DiscriminatorValue("RECEPTIONIST")
public class Receptionist extends StaffMember {

    @Column(name = "desk_number")
    private Integer deskNumber;

    @Column(name = "hours_per_week")
    private Integer hoursPerWeek;

    // ---- Getters & setters --------------------------------------------------

    public Integer getDeskNumber()                      { return deskNumber; }
    public void setDeskNumber(Integer deskNumber)       { this.deskNumber = deskNumber; }

    public Integer getHoursPerWeek()                    { return hoursPerWeek; }
    public void setHoursPerWeek(Integer hoursPerWeek)   { this.hoursPerWeek = hoursPerWeek; }
}
