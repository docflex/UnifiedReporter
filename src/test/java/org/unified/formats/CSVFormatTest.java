package org.unified.formats;

import org.junit.jupiter.api.Test;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CSVFormatTest {

    @Test
    void testValidCsvParsing() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/valid.csv");
        CSVFormat parser = new CSVFormat(inputStream, "ValidCSV");

        List<Map<String, Object>> rows = parser.getDataRows();
        assertEquals(2, rows.size());
        assertEquals("Rehber", rows.get(0).get("Name"));
        assertEquals(List.of("Name", "Age", "Score"), parser.getColumnOrder());
    }

    @Test
    void testEmptyHeaderLineThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/no_headers.csv");
        FormatException exception = assertThrows(FormatException.class,
                () -> new CSVFormat(inputStream, "MissingHeader"));
        assertEquals(ErrorCode.CSV_HEADER_EMPTY, exception.getErrorCode());
    }

    @Test
    void testEmptyHeaderCellsThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/empty_header.csv");
        FormatException exception = assertThrows(FormatException.class,
                () -> new CSVFormat(inputStream, "EmptyHeader"));
        assertEquals(ErrorCode.CSV_HEADER_EMPTY, exception.getErrorCode());
    }

    @Test
    void testDuplicateHeaderThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/duplicate_headers.csv");
        FormatException exception = assertThrows(FormatException.class,
                () -> new CSVFormat(inputStream, "DuplicateHeader"));
        assertEquals(ErrorCode.CSV_HEADER_DUPLICATE, exception.getErrorCode());
    }

    @Test
    void testMismatchedRowThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/mismatched_row.csv");
        FormatException exception = assertThrows(FormatException.class,
                () -> new CSVFormat(inputStream, "MismatchRow"));
        assertEquals(ErrorCode.CSV_ROW_COLUMN_MISMATCH, exception.getErrorCode());
    }

    @Test
    void testMalformedQuotedCsvThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/malformed.csv");
        FormatException exception = assertThrows(FormatException.class,
                () -> new CSVFormat(inputStream, "MalformedCSV"));
        assertEquals(ErrorCode.CSV_PARSE_ERROR, exception.getErrorCode());
    }

    @Test
    void testInternationalHeadersSupport() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/international_headers.csv");
        CSVFormat parser = new CSVFormat(inputStream, "IntlHeaders");

        assertEquals(List.of("名", "Ålder", "Score"), parser.getColumnOrder());

        List<Map<String, Object>> rows = parser.getDataRows();
        assertFalse(rows.isEmpty());
        assertTrue(rows.get(0).containsKey("Ålder"));
    }

    @Test
    void testQuotedCommaSupport() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/quoted_commas.csv");
        CSVFormat parser = new CSVFormat(inputStream, "Quoted");

        List<Map<String, Object>> rows = parser.getDataRows();
        assertEquals("Smith, John", rows.get(0).get("Name"));
    }

    @Test
    void testSourceNameFallback() {
        InputStream inputStream = getClass().getResourceAsStream("/CSV/valid.csv");
        CSVFormat parser = new CSVFormat(inputStream, null);
        assertEquals("CSV", parser.getSourceName());
    }
}
