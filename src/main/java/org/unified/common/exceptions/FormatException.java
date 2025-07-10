package org.unified.common.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.unified.common.enums.ErrorCode;

@Getter
@Slf4j
public class FormatException extends RuntimeException {
    private final ErrorCode errorCode;
    public FormatException(ErrorCode errorCode, Exception parentException) {
        super(parentException + "\nDescription:\n" + errorCode.getMessage());
        log.error("Format Exception Occurred!");
        log.error("Error Code: {} | Error Message: {} | Error Status: {}", errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
