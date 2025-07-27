package org.unified.formats;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.InputStream;
import java.util.*;

/**
 * A parser class that implements {@link UnifiedFormat} for reading Excel (XLSX) files.
 * <p>
 * This class uses Apache POI to extract data from the first sheet of an XLSX file.
 * It validates headers and processes each row into a list of maps.
 */
@Slf4j
public class XLSXFormat implements UnifiedFormat {

    private final List<Map<String, Object>> dataRows = new ArrayList<>();
    private final List<String> columnOrder = new ArrayList<>();
    private final String sourceName;

    /**
     * Constructs an {@code XLSXFormat} parser from an {@link InputStream}.
     *
     * @param xlsxStream the input stream of the XLSX file
     * @param sourceName optional logical name for the file (used in logs); defaults to "XLSX" if null
     */
    public XLSXFormat(InputStream xlsxStream, String sourceName) {
        this.sourceName = sourceName != null ? sourceName : "XLSX";
        parse(xlsxStream);
    }

    /**
     * Returns the list of parsed data rows from the XLSX sheet.
     * Each row is represented as a {@link Map} with headers as keys.
     *
     * @return list of data rows
     */
    @Override
    public List<Map<String, Object>> getDataRows() {
        return dataRows;
    }

    /**
     * Returns the column order as defined in the Excel header row.
     *
     * @return list of header names
     */
    @Override
    public List<String> getColumnOrder() {
        return columnOrder;
    }

    /**
     * Returns the name of the source Excel file.
     *
     * @return source name
     */
    @Override
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Parses the XLSX input stream using Apache POI.
     * Extracts headers and rows, and populates internal data structures.
     *
     * @param inputStream the input stream of the XLSX file
     * @throws FormatException if parsing fails due to invalid structure or I/O error
     */
    private void parse(InputStream inputStream) {
        log.info("Starting Parsing XLSX ---> UnifiedFormat");
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // or by name
            Iterator<Row> rowIterator = sheet.iterator();

            extractHeadersFromExcel(rowIterator);
            processRowsFromExcel(rowIterator);

        } catch (FormatException e) {
            log.error("❌ Format error while parsing XLSX file", e);
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error while parsing XLSX file", e);
            throw new FormatException(ErrorCode.XLSX_PARSE_ERROR, e);
        }
    }

    // Utility Functions

    /**
     * Extracts the value from an Excel cell based on its type.
     *
     * @param cell the cell from which to extract value
     * @return the extracted Java object value (String, Double, Boolean, Date, or null)
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue()
                    : cell.getNumericCellValue();
            case FORMULA -> cell.getCellFormula();
            case BLANK, ERROR, _NONE -> null;
        };
    }

    /**
     * Extracts headers from the first row of the Excel sheet.
     * Validates for text type, non-blank values, and uniqueness.
     *
     * @param rowIterator iterator starting at the first row of the sheet
     * @throws FormatException if headers are missing, duplicated, or invalid
     */
    private void extractHeadersFromExcel(Iterator<Row> rowIterator) {
        if (!rowIterator.hasNext()) {
            log.error("❌ Missing headers: sheet is empty");
            throw new FormatException(ErrorCode.XLSX_MISSING_HEADERS, new Exception("Missing headers: the sheet is empty"));
        }
        Row headerRow = rowIterator.next();

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            validateExcelHeaders(cell, i);
            columnOrder.add(cell.getStringCellValue().trim());
        }

        log.info("Extracted headers: {}", columnOrder);
    }

    /**
     * Validates and extracts each row of data from the Excel sheet.
     * Adds them as maps to the internal list of data rows.
     *
     * @param rowIterator iterator positioned after the header row
     * @throws FormatException if processing fails
     */
    private void processRowsFromExcel(Iterator<Row> rowIterator) {
        int rowIndex = 1;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, Object> rowMap = new LinkedHashMap<>();

            for (int i = 0; i < columnOrder.size(); i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                rowMap.put(columnOrder.get(i), getCellValue(cell));
            }

            dataRows.add(rowMap);
            log.debug("Processed row {}: {}", rowIndex++, rowMap);
        }
    }

    /**
     * Validates the header cell:
     * <ul>
     *     <li>Must be of type {@code STRING}</li>
     *     <li>Must not be blank</li>
     *     <li>Must be unique</li>
     * </ul>
     *
     * @param header the header cell to validate
     * @param index  the column index (used for logging)
     * @throws FormatException if any condition fails
     */
    private void validateExcelHeaders(Cell header, int index) {
        if (header == null || header.getCellType() != CellType.STRING) {
            String msg = "Header at column " + index + " must be text. Found: " +
                    (header == null ? "null" : header.getCellType());
            log.error("❌ {}", msg);
            throw new FormatException(ErrorCode.XLSX_INVALID_HEADER_TYPE, new Exception(msg));
        }

        String headerString = header.getStringCellValue().trim();
        if (headerString.isBlank()) {
            String msg = "Header at column index " + index + " is blank";
            log.error("❌ {}", msg);
            throw new FormatException(ErrorCode.XLSX_NULL_HEADER, new Exception(msg));
        }

        if (columnOrder.contains(headerString)) {
            String msg = "Duplicate header: '" + headerString + "' at index " + index;
            log.error("❌ {}", msg);
            throw new FormatException(ErrorCode.XLSX_DUPLICATE_HEADER, new IllegalArgumentException(msg));
        }
    }
}
