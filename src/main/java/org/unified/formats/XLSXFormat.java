package org.unified.formats;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.InputStream;
import java.util.*;

@Slf4j
public class XLSXFormat implements UnifiedFormat {

    private final List<Map<String, Object>> dataRows = new ArrayList<>();
    private final List<String> columnOrder = new ArrayList<>();
    private final String sourceName;

    public XLSXFormat(InputStream xlsxStream, String sourceName) {
        this.sourceName = sourceName != null ? sourceName : "XLSX";
        parse(xlsxStream);
    }

    @Override
    public List<Map<String, Object>> getDataRows() {
        return dataRows;
    }

    @Override
    public List<String> getColumnOrder() {
        return columnOrder;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    public void parse(InputStream inputStream) {
        log.info("Starting Parsing XLSX ---> UnifiedFormat");
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // or by name
            Iterator<Row> rowIterator = sheet.iterator();

            extractHeadersFromExcel(rowIterator);
            processRowsFromExcel(rowIterator);

        } catch (FormatException e) {
            throw e;
        } catch (Exception e) {
            throw new FormatException(ErrorCode.XLSX_PARSE_ERROR, e);
        }
    }

    // Utility Functions

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

    private void extractHeadersFromExcel(Iterator<Row> rowIterator) {
        if (!rowIterator.hasNext()) {
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

    private void validateExcelHeaders(Cell header, int index) {
        if (header == null || header.getCellType() != CellType.STRING) {
            throw new FormatException(ErrorCode.XLSX_INVALID_HEADER_TYPE,
                    new Exception("Header at column " + index + " must be text, found: " +
                            (header == null ? "null" : header.getCellType())));
        }
        String headerString = header.getStringCellValue().trim();
        if (headerString.isBlank()) {
            throw new FormatException(ErrorCode.XLSX_NULL_HEADER,
                    new Exception("Header at column index " + index + " is blank"));
        }
        if (columnOrder.contains(headerString)) {
            throw new FormatException(ErrorCode.XLSX_DUPLICATE_HEADER,
                    new IllegalArgumentException("Duplicate header: '" + header + "' at index " + index));
        }
    }
}
