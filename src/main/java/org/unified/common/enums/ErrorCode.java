package org.unified.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // CSV ERRORS
    CSV_PARSE_ERROR("CSV_001", "Failed to parse CSV file", HttpStatus.BAD_REQUEST),
    CSV_IO_ERROR("CSV_002", "Failed to read CSV input", HttpStatus.UNPROCESSABLE_ENTITY),
    CSV_ROW_COLUMN_MISMATCH("CSV_003", "Issue with Row Column Mismatch", HttpStatus.NOT_ACCEPTABLE),
    CSV_HEADER_MISSING("CSV_004", "CSV header is missing or null", HttpStatus.BAD_REQUEST),
    CSV_HEADER_EMPTY("CSV_005", "CSV header does not contain any valid columns", HttpStatus.BAD_REQUEST),
    CSV_HEADER_DUPLICATE("CSV_006", "CSV header contains duplicate columns", HttpStatus.BAD_REQUEST),


    // XLSX ERRORS
    XLSX_PARSE_ERROR("XLSX_001", "Failed to read Excel file", HttpStatus.BAD_REQUEST),
    XLSX_MISSING_HEADERS("XLSX_002", "XLSX file is empty.", HttpStatus.NO_CONTENT),
    XLSX_NULL_HEADER("XLSX_003", "Found blank or null header.", HttpStatus.PARTIAL_CONTENT),
    XLSX_DUPLICATE_HEADER("XLSX_004", "Duplicate header Found.", HttpStatus.NOT_ACCEPTABLE),
    XLSX_INVALID_HEADER_TYPE("XLSX_005", "Invalid Header Found.", HttpStatus.NOT_ACCEPTABLE),

    // JSON ERRORS
    JSON_SYNTAX_ERROR("JSON_001", "Malformed JSON input", HttpStatus.BAD_REQUEST),
    JSON_MAPPING_ERROR("JSON_002", "Could not map JSON to expected structure", HttpStatus.UNPROCESSABLE_ENTITY),

    // BYTE STREAM ERRORS
    BYTE_UNSUPPORTED_FORMAT("BYTE_001", "Unsupported byte stream format", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    BYTE_DECODE_ERROR("BYTE_002", "Failed to decode byte stream", HttpStatus.BAD_REQUEST),

    // GENERIC
    UNKNOWN_ERROR("GEN_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    IO_EXCEPTION("GEN_002", "IO Exception Occured for Input", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
