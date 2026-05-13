/**
 * TypeScript types that mirror the backend DTOs exactly.
 * Keeping them in sync ensures type safety across the network boundary.
 */

export type StaffType = "DOCTOR" | "RECEPTIONIST";

/** Matches the backend's StaffMemberResponse record */
export interface StaffMember {
  type: StaffType;
  staffId: string;
  name: string;
  surname: string;
  dob: string | null;        // ISO date string "yyyy-MM-dd"
  phoneNo: string | null;
  // Doctor-specific (null for Receptionist)
  licenceNumber: string | null;
  specialisation: string | null;
  consultationsPerWeek: number | null;
  // Receptionist-specific (null for Doctor)
  deskNumber: number | null;
  hoursPerWeek: number | null;
}

/** Matches the backend's StatsResponse record */
export interface StaffStats {
  total: number;
  doctors: number;
  receptionists: number;
  limit: number;
  remainingCapacity: number;
}

/** Matches the backend's CreateDoctorRequest record */
export interface CreateDoctorRequest {
  name: string;
  surname: string;
  dob: string | null;
  phoneNo: string | null;
  staffId: string;
  licenceNumber: string | null;
  specialisation: string | null;
  consultationsPerWeek: number;
}

/** Matches the backend's CreateReceptionistRequest record */
export interface CreateReceptionistRequest {
  name: string;
  surname: string;
  dob: string | null;
  phoneNo: string | null;
  staffId: string;
  deskNumber: number;
  hoursPerWeek: number;
}

/** Matches the backend's ErrorResponse record */
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
}
