package org.unified.formats;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XLSXFormatTest {
    InputStream is = getClass().getResourceAsStream("/XLSX/valid.xlsx");
    private final XLSXFormat format = new XLSXFormat(is, "Test");


    @Test
    void testStringCellValue() {
        Cell cell = createCell(CellType.STRING);
        cell.setCellValue("Hello");
        assertEquals("Hello", format.getCellValue(cell));
    }

    @Test
    void testBooleanCellValue() {
        Cell cell = createCell(CellType.BOOLEAN);
        cell.setCellValue(true);
        assertEquals(true, format.getCellValue(cell));
    }

    @Test
    void testNumericCellValue() {
        Cell cell = createCell(CellType.NUMERIC);
        cell.setCellValue(42.0);
        assertEquals(42.0, format.getCellValue(cell));
    }

    @Test
    void testDateCellValue() {
        Cell cell = createCell(CellType.NUMERIC);
        Date date = new Date();
        cell.setCellValue(date);
        CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
        style.setDataFormat((short) 14); // e.g., "m/d/yy"
        cell.setCellStyle(style);

        DataFormat df = cell.getSheet().getWorkbook().createDataFormat();
        style.setDataFormat(df.getFormat("m/d/yy"));

        assertEquals(date, format.getCellValue(cell));
    }

    @Test
    void testFormulaCellValue() {
        Cell cell = createCell(CellType.FORMULA);
        cell.setCellFormula("SUM(A1:A2)");
        assertEquals("SUM(A1:A2)", format.getCellValue(cell));
    }

    @Test
    void testBlankCellValue() {
        Cell cell = createCell(CellType.BLANK);
        assertNull(format.getCellValue(cell));
    }

    @Test
    void testErrorCellValue() {
        Cell cell = createCell(CellType.ERROR);
        assertNull(format.getCellValue(cell));
    }

    @Test
    void testNoneCellTypeReturnsNull() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        assertEquals(CellType.BLANK, cell.getCellType());

        assertNull(format.getCellValue(cell));
    }


    @Test
    void testNullCellReturnsNull() {
        assertNull(format.getCellValue(null));
    }

    // Helper method to create a cell of given type
    private Cell createCell(CellType type) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        return row.createCell(0, type);
    }

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
        FormatException exception = assertThrows(FormatException.class,
                () -> new XLSXFormat(inputStream, "CorruptFile"));
        assertEquals(ErrorCode.XLSX_PARSE_ERROR, exception.getErrorCode());
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
