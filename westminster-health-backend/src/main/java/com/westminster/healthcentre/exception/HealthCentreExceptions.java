package com.westminster.healthcentre.exception;

/**
 * Domain exceptions for the Westminster Health Centre API.
 *
 * <p>Each exception maps to a specific HTTP status code in
 * {@link GlobalExceptionHandler}. Using typed exceptions rather than generic
 * {@code RuntimeException} means the handler can respond with the correct
 * status without inspecting messages or adding if-else chains.
 */
public final class HealthCentreExceptions {

    private HealthCentreExceptions() {}

    // -------------------------------------------------------------------------

    /** Thrown when a lookup by staffId finds no matching record (HTTP 404). */
    public static class StaffNotFoundException extends RuntimeException {
        public StaffNotFoundException(String staffId) {
            super("No staff member found with ID '" + staffId + "'.");
        }
    }

    // -------------------------------------------------------------------------

    /** Thrown when the configured staff limit has been reached (HTTP 409). */
    public static class StaffLimitReachedException extends RuntimeException {
        public StaffLimitReachedException(int limit) {
            super("Staff limit of " + limit + " has been reached. Remove a member before adding another.");
        }
    }

    // -------------------------------------------------------------------------

    /** Thrown when a new member's staffId is already in use (HTTP 409). */
    public static class DuplicateStaffIdException extends RuntimeException {
        public DuplicateStaffIdException(String staffId) {
            super("A staff member with ID '" + staffId + "' is already registered.");
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Thrown when a PUT request targets the wrong endpoint for the staff
     * member's actual role — e.g. sending a doctor update to the receptionist
     * endpoint (HTTP 400).
     */
    public static class RoleMismatchException extends RuntimeException {
        public RoleMismatchException(String staffId, String expectedRole) {
            super("Staff member '" + staffId + "' is not a " + expectedRole + ".");
        }
    }
}
