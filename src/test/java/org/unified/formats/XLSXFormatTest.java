package org.unified.formats;

import org.junit.jupiter.api.Test;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XLSXFormatTest {

    @Test
    void testValidXlsxParsing() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/valid.xlsx");
        XLSXFormat parser = new XLSXFormat(inputStream, "ValidXLSX");

        List<Map<String, Object>> rows = parser.getDataRows();
        assertEquals(2, rows.size());
        assertEquals("Rehber", rows.get(0).get("Name"));
        assertEquals(List.of("Name", "Age", "Score"), parser.getColumnOrder());
    }

    @Test
    void testEmptyHeaders() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/no_headers.xlsx");
        FormatException exception = assertThrows(FormatException.class,
                () -> new XLSXFormat(inputStream, "Invalid"));
        assertEquals(ErrorCode.XLSX_NULL_HEADER, exception.getErrorCode());
    }

    @Test
    void testMismatchedRowSize() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/empty_file.xlsx");
        FormatException exception = assertThrows(FormatException.class,
                () -> new XLSXFormat(inputStream, "BadRow"));
        assertEquals(ErrorCode.XLSX_MISSING_HEADERS, exception.getErrorCode());
    }

    @Test
    void testDuplicateHeaders() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/duplicate_headers.xlsx");
        FormatException exception = assertThrows(FormatException.class,
                () -> new XLSXFormat(inputStream, "DuplicateHeader"));
        assertEquals(ErrorCode.XLSX_DUPLICATE_HEADER, exception.getErrorCode());
    }

//    @Test
//    void testValidationMissingRequiredField() {
//        InputStream inputStream = getClass().getResourceAsStream("/missing_employee_id.xlsx");
//        XLSXFormat parser = new XLSXFormat(inputStream, "MissingField");
//        FormatException exception = assertThrows(FormatException.class, parser::validateFields);
//        assertEquals(ErrorCode.XLSX_VALIDATION_FAILED, exception.getErrorCode());
//    }

//    @Test
//    void testValidationIncorrectType() {
//        InputStream inputStream = getClass().getResourceAsStream("/invalid_age_type.xlsx");
//        XLSXFormat parser = new XLSXFormat(inputStream, "BadType");
//        FormatException exception = assertThrows(FormatException.class, parser::validateFields);
//        assertEquals(ErrorCode.XLSX_VALIDATION_FAILED, exception.getErrorCode());
//    }

    @Test
    void testSourceNameFallback() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/valid.xlsx");
        XLSXFormat parser = new XLSXFormat(inputStream, null);
        assertEquals("XLSX", parser.getSourceName());
    }
}
