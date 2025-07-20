package org.unified.common.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.unified.common.enums.ErrorCode;

@Getter
@Slf4j
public class ReportException extends RuntimeException {
    private final ErrorCode errorCode;

    public ReportException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        logError(null);
    }

    public ReportException(ErrorCode errorCode, Exception parentException) {
        super(buildMessage(errorCode, parentException), parentException);
        this.errorCode = errorCode;
        logError(parentException);
    }

    private static String buildMessage(ErrorCode errorCode, Exception parent) {
        if (parent == null) {
            return errorCode.getMessage();
        }
        return parent + "\nDescription:\n" + errorCode.getMessage();
    }

    private void logError(Exception parentException) {
        log.error("Report Exception Occurred!");
        log.error("Error Code: {} | Error Message: {} | HTTP Status: {}",
                errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
        if (parentException != null) {
            log.error("Cause: ", parentException);
        }
    }
}
