package org.unified.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum representing structured error codes used across the application.
 * Each error code includes:
 * <ul>
 *   <li>A unique identifier (e.g., CSV_001)</li>
 *   <li>A human-readable error message</li>
 *   <li>An associated HTTP status code</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ================= CSV ERRORS =================

    /**
     * Error when CSV parsing fails due to malformed content.
     */
    CSV_PARSE_ERROR("CSV_001", "Failed to parse CSV file", HttpStatus.BAD_REQUEST),

    /**
     * Error when reading the CSV input fails.
     */
    CSV_IO_ERROR("CSV_002", "Failed to read CSV input", HttpStatus.UNPROCESSABLE_ENTITY),

    /**
     * Error when the number of columns in a row doesn't match the header.
     */
    CSV_ROW_COLUMN_MISMATCH("CSV_003", "Issue with Row Column Mismatch", HttpStatus.NOT_ACCEPTABLE),

    /**
     * Error when the CSV file does not have a header row.
     */
    CSV_HEADER_MISSING("CSV_004", "CSV header is missing or null", HttpStatus.BAD_REQUEST),

    /**
     * Error when the CSV header is present but has no valid column names.
     */
    CSV_HEADER_EMPTY("CSV_005", "CSV header does not contain any valid columns", HttpStatus.BAD_REQUEST),

    /**
     * Error when the CSV header contains duplicate column names.
     */
    CSV_HEADER_DUPLICATE("CSV_006", "CSV header contains duplicate columns", HttpStatus.BAD_REQUEST),

    // ================= XLSX ERRORS =================

    /**
     * Error when parsing the Excel file fails.
     */
    XLSX_PARSE_ERROR("XLSX_001", "Failed to read Excel file", HttpStatus.BAD_REQUEST),

    /**
     * Error when the Excel file is completely empty.
     */
    XLSX_MISSING_HEADERS("XLSX_002", "XLSX file is empty.", HttpStatus.NO_CONTENT),

    /**
     * Error when the Excel header contains null or blank cells.
     */
    XLSX_NULL_HEADER("XLSX_003", "Found blank or null header.", HttpStatus.PARTIAL_CONTENT),

    /**
     * Error when the Excel header contains duplicate column names.
     */
    XLSX_DUPLICATE_HEADER("XLSX_004", "Duplicate header Found.", HttpStatus.NOT_ACCEPTABLE),

    /**
     * Error when an Excel header has an unsupported or invalid type.
     */
    XLSX_INVALID_HEADER_TYPE("XLSX_005", "Invalid Header Found.", HttpStatus.NOT_ACCEPTABLE),

    // ================= JSON ERRORS =================

    /**
     * Error when the JSON input is malformed or contains syntax issues.
     */
    JSON_SYNTAX_ERROR("JSON_001", "Malformed JSON input", HttpStatus.BAD_REQUEST),

    /**
     * Error when the JSON structure cannot be mapped to the expected format.
     */
    JSON_MAPPING_ERROR("JSON_002", "Could not map JSON to expected structure", HttpStatus.UNPROCESSABLE_ENTITY),

    // ================= BYTE STREAM ERRORS =================

    /**
     * Error when the byte stream format is not recognized or unsupported.
     */
    BYTE_UNSUPPORTED_FORMAT("BYTE_001", "Unsupported byte stream format", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    /**
     * Error when decoding a byte stream fails.
     */
    BYTE_DECODE_ERROR("BYTE_002", "Failed to decode byte stream", HttpStatus.BAD_REQUEST),

    // ================= GENERIC ERRORS =================

    /**
     * Fallback error for unknown or unexpected situations.
     */
    UNKNOWN_ERROR("GEN_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * Error when an I/O exception occurs while processing input.
     */
    IO_EXCEPTION("GEN_002", "IO Exception Occured for Input", HttpStatus.NOT_FOUND),

    // ================= REPORT GENERATION ERRORS =================

    /**
     * Error when the report template stream is null.
     */
    REPORT_TEMPLATE_NULL("REP_001", "Report template InputStream is null", HttpStatus.BAD_REQUEST),

    /**
     * Error when compiling the JasperReports .jrxml template fails.
     */
    REPORT_TEMPLATE_COMPILE_FAILED("REP_002", "Failed to compile .jrxml template", HttpStatus.UNPROCESSABLE_ENTITY),

    /**
     * Error when loading a compiled Jasper .jasper file fails.
     */
    REPORT_TEMPLATE_LOAD_FAILED("REP_003", "Failed to load compiled .jasper template", HttpStatus.UNPROCESSABLE_ENTITY),

    /**
     * Error when the input data for report generation is missing or empty.
     */
    REPORT_DATA_EMPTY("REP_004", "Input data list is empty or null", HttpStatus.NO_CONTENT),

    /**
     * Error when filling the report with data and parameters fails.
     */
    REPORT_FILL_FAILED("REP_005", "Failed to fill the report with provided data and parameters", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * Error when exporting the report fails due to I/O or formatting issues.
     */
    REPORT_EXPORT_FAILED("REP_006", "Failed to export the report to the selected format", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * Error when an unsupported report export format is specified.
     */
    REPORT_FORMAT_UNSUPPORTED("REP_007", "Unsupported export format", HttpStatus.NOT_ACCEPTABLE);

    /**
     * A unique string code identifying the error.
     */
    private final String code;

    /**
     * A human-readable error message describing the issue.
     */
    private final String message;

    /**
     * The HTTP status code associated with the error.
     */
    private final HttpStatus httpStatus;
}
