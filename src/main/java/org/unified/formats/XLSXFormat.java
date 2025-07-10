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

    public void parse(InputStream inputStream) {
        log.info("Starting Parsing XLSX ---> UnifiedFormat");
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // or by name
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new FormatException(ErrorCode.XLSX_MISSING_HEADERS, new Exception());
            }

            // First row = headers
            Row headerRow = rowIterator.next();
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue();
                if (header == null || header.isBlank()) {
                    throw new FormatException(ErrorCode.XLSX_NULL_HEADER, new Exception());
                }
                if (columnOrder.contains(header)) {
                    throw new FormatException(ErrorCode.XLSX_DUPLICATE_HEADER, new Exception("Duplicate header: " + header));
                }
                columnOrder.add(header);
            }

            // Process data rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowMap = new LinkedHashMap<>();

                for (int i = 0; i < columnOrder.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowMap.put(columnOrder.get(i), getCellValue(cell));
                }

                dataRows.add(rowMap);
            }

        } catch (FormatException e) {
            throw e;
        } catch (Exception e) {
            throw new FormatException(ErrorCode.XLSX_PARSE_ERROR, e);
        }
    }

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
}
