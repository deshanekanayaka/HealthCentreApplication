import type {
  StaffMember,
  StaffStats,
  CreateDoctorRequest,
  CreateReceptionistRequest,
  ApiError,
} from "@/types/staff";

/**
 * Base URL is driven by an environment variable so the same build works in
 * development (localhost:8080) and in production (the Railway URL).
 *
 * In Vite, env vars must be prefixed with VITE_ to be accessible in the browser.
 * Set VITE_API_URL in a .env.local file locally, or in Vercel's environment
 * variables for production.
 */
const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// ---- Error handling ---------------------------------------------------------

/**
 * Reads the response. If the status is not OK, parses the backend's
 * ErrorResponse envelope and throws it as a typed Error.
 */
async function handleResponse<T>(res: Response): Promise<T> {
  if (res.ok) {
    // 204 No Content has no body
    if (res.status === 204) return undefined as T;
    return res.json() as Promise<T>;
  }

  let errorMessage = `Request failed with status ${res.status}`;
  try {
    const body: ApiError = await res.json();
    errorMessage = body.message ?? errorMessage;
  } catch {
    // Response body wasn't JSON — use the generic message
  }
  throw new Error(errorMessage);
}

// ---- API functions ----------------------------------------------------------

/** Returns all staff, optionally filtered by a search term. */
export async function fetchAllStaff(search?: string): Promise<StaffMember[]> {
  const url = new URL(`${BASE_URL}/api/staff`);
  if (search?.trim()) url.searchParams.set("search", search.trim());
  const res = await fetch(url.toString());
  return handleResponse<StaffMember[]>(res);
}

/** Returns summary statistics. */
export async function fetchStats(): Promise<StaffStats> {
  const res = await fetch(`${BASE_URL}/api/staff/stats`);
  return handleResponse<StaffStats>(res);
}

/** Adds a new doctor. */
export async function createDoctor(
  data: CreateDoctorRequest
): Promise<StaffMember> {
  const res = await fetch(`${BASE_URL}/api/staff/doctors`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return handleResponse<StaffMember>(res);
}

/** Adds a new receptionist. */
export async function createReceptionist(
  data: CreateReceptionistRequest
): Promise<StaffMember> {
  const res = await fetch(`${BASE_URL}/api/staff/receptionists`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return handleResponse<StaffMember>(res);
}

/** Updates an existing doctor. */
export async function updateDoctor(
  staffId: string,
  data: CreateDoctorRequest
): Promise<StaffMember> {
  const res = await fetch(`${BASE_URL}/api/staff/doctors/${staffId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return handleResponse<StaffMember>(res);
}

/** Updates an existing receptionist. */
export async function updateReceptionist(
  staffId: string,
  data: CreateReceptionistRequest
): Promise<StaffMember> {
  const res = await fetch(`${BASE_URL}/api/staff/receptionists/${staffId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return handleResponse<StaffMember>(res);
}

/** Deletes a staff member by staffId. */
export async function deleteStaff(staffId: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/api/staff/${staffId}`, {
    method: "DELETE",
  });
  return handleResponse<void>(res);
}
