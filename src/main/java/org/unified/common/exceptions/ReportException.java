package org.unified.common.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.unified.common.enums.ErrorCode;

/**
 * Custom runtime exception used to represent errors related to report generation
 * (e.g., loading templates, compiling, filling, or exporting reports).
 * <p>
 * Each exception wraps an {@link ErrorCode} that provides detailed context about the issue,
 * including an appropriate HTTP status for RESTful responses.
 */
@Getter
@Slf4j
public class ReportException extends RuntimeException {

    /**
     * The structured error code describing the type of report-related error.
     */
    private final ErrorCode errorCode;

    /**
     * Constructs a new {@code ReportException} using only an error code.
     * Use this when there is no underlying cause.
     *
     * @param errorCode the specific {@link ErrorCode} that describes the error
     */
    public ReportException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        logError(null);
    }

    /**
     * Constructs a new {@code ReportException} with an error code and a cause.
     *
     * @param errorCode       the specific {@link ErrorCode} that describes the error
     * @param parentException the underlying exception that caused this error
     */
    public ReportException(ErrorCode errorCode, Exception parentException) {
        super(buildMessage(errorCode, parentException), parentException);
        this.errorCode = errorCode;
        logError(parentException);
    }

    /**
     * Builds a complete exception message including the parent exception's message.
     *
     * @param errorCode the associated error code
     * @param parent    the underlying exception (if any)
     * @return a detailed error message
     */
    private static String buildMessage(ErrorCode errorCode, Exception parent) {
        if (parent == null) {
            return errorCode.getMessage();
        }
        return parent + "\nDescription:\n" + errorCode.getMessage();
    }

    /**
     * Logs the error details using SLF4J.
     *
     * @param parentException the original exception, if present
     */
    private void logError(Exception parentException) {
        log.error("Report Exception Occurred!");
        log.error("Error Code: {} | Error Message: {} | HTTP Status: {}",
                errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
        if (parentException != null) {
            log.error("Cause: ", parentException);
        }
    }
}
