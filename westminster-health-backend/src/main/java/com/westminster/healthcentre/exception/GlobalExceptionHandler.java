package com.westminster.healthcentre.exception;

import com.westminster.healthcentre.dto.StaffDtos.ErrorResponse;
import com.westminster.healthcentre.exception.HealthCentreExceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralised exception → HTTP response mapping.
 *
 * <p>{@code @RestControllerAdvice} intercepts exceptions thrown from any
 * controller and converts them to a consistent JSON error envelope
 * ({@link ErrorResponse}) before the response is written. Controllers
 * themselves contain no error-handling logic.
 *
 * <p><strong>Status mapping:</strong>
 * <ul>
 *   <li>404 NOT_FOUND        → {@link StaffNotFoundException}
 *   <li>409 CONFLICT         → {@link StaffLimitReachedException}, {@link DuplicateStaffIdException}
 *   <li>400 BAD_REQUEST      → {@link RoleMismatchException}, Bean Validation failures
 *   <li>500 INTERNAL_SERVER_ERROR → any other uncaught exception
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StaffNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(StaffNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({StaffLimitReachedException.class, DuplicateStaffIdException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RoleMismatchException.class)
    public ResponseEntity<ErrorResponse> handleRoleMismatch(RoleMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles {@code @Valid} failures on request bodies.
     * Collects all field errors into one readable message rather than
     * returning only the first violation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    /** Safety net — logs the cause and returns a generic 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // In production, log ex here (e.g. via SLF4J)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    // ---- Helper -------------------------------------------------------------

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status.value(), message, Instant.now().toString()));
    }
}
