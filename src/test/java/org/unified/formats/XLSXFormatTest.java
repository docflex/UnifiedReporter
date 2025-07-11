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

    @Test
    void testMalformedXlsxFileThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/malformed.xlsx");
        assertThrows(FormatException.class, () -> new XLSXFormat(inputStream, "CorruptFile"));
    }

    @Test
    void testEmptyHeaderThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/empty_header.xlsx");
        FormatException exception = assertThrows(FormatException.class, () -> new XLSXFormat(inputStream, "EmptyHeader"));
        assertEquals(ErrorCode.XLSX_NULL_HEADER, exception.getErrorCode());
    }

    @Test
    void testDuplicateHeadersThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/duplicate_headers.xlsx");
        FormatException exception = assertThrows(FormatException.class, () -> new XLSXFormat(inputStream, "DuplicateHeader"));
        assertEquals(ErrorCode.XLSX_DUPLICATE_HEADER, exception.getErrorCode());
    }

    @Test
    void testNonTextHeadersRowThrowsException() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/numeric_header.xlsx");
        FormatException exception = assertThrows(FormatException.class, () -> new XLSXFormat(inputStream, "NoHeaders"));
        assertEquals(ErrorCode.XLSX_INVALID_HEADER_TYPE, exception.getErrorCode());
    }

    @Test
    void testInternationalHeadersSupport() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/international_headers.xlsx");
        XLSXFormat parser = new XLSXFormat(inputStream, "IntlHeaders");

        assertEquals(List.of("名", "Ålder", "Score"), parser.getColumnOrder());

        List<Map<String, Object>> rows = parser.getDataRows();
        assertFalse(rows.isEmpty());
        assertTrue(rows.get(0).containsKey("Ålder"));
    }


    @Test
    void testSourceNameFallback() {
        InputStream inputStream = getClass().getResourceAsStream("/XLSX/valid.xlsx");
        XLSXFormat parser = new XLSXFormat(inputStream, null);
        assertEquals("XLSX", parser.getSourceName());
    }
}
