package org.unified.common.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.unified.common.enums.ErrorCode;

/**
 * Custom runtime exception used to handle structured format-related errors
 * in parsing or exporting operations such as CSV, XLSX, JSON, etc.
 * <p>
 * Each exception is associated with an {@link ErrorCode} that describes the nature
 * of the error and the appropriate HTTP status for RESTful response handling.
 */
@Getter
@Slf4j
public class FormatException extends RuntimeException {

    /**
     * The structured error code representing the type of format exception.
     */
    private final ErrorCode errorCode;

    /**
     * Constructs a new {@code FormatException} with the given error code.
     * This constructor is used when no underlying exception is present.
     *
     * @param errorCode the specific {@link ErrorCode} representing the error
     */
    public FormatException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        logError(null);
    }

    /**
     * Constructs a new {@code FormatException} with the given error code
     * and the original cause of the error.
     *
     * @param errorCode       the specific {@link ErrorCode} representing the error
     * @param parentException the underlying cause of this exception
     */
    public FormatException(ErrorCode errorCode, Exception parentException) {
        super(buildMessage(errorCode, parentException), parentException);
        this.errorCode = errorCode;
        logError(parentException);
    }

    /**
     * Builds a detailed error message including the cause and description.
     *
     * @param errorCode the error code associated with this exception
     * @param parent    the underlying exception
     * @return a formatted error message
     */
    private static String buildMessage(ErrorCode errorCode, Exception parent) {
        if (parent == null) {
            return errorCode.getMessage();
        }
        return parent + "\nDescription:\n" + errorCode.getMessage();
    }

    /**
     * Logs error details to the logger.
     *
     * @param parentException the original exception, if any
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
