package org.unified.formats;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
public class CSVFormat implements UnifiedFormat {

    private final List<Map<String, Object>> dataRows = new ArrayList<>();
    private final List<String> columnOrder = new ArrayList<>();
    private final String sourceName;

    public CSVFormat(InputStream csvStream, String sourceName) {
        this.sourceName = sourceName != null ? sourceName : "CSV";
        parse(csvStream);
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
        log.info("Starting Parsing CSV ---> UnifiedFormat");
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] headerLine = reader.readNext();

            extractHeadersFromCSV(headerLine);
            processRowsFromCSV(reader);

        } catch (FormatException e) {
            throw e;
        } catch (Exception e) {
            throw new FormatException(ErrorCode.CSV_PARSE_ERROR, e);
        }
    }

    // Utility Functions

    private void extractHeadersFromCSV(String[] headerLine) {
        if (headerLine == null || headerLine.length == 0) {
            throw new FormatException(ErrorCode.CSV_HEADER_MISSING, new Exception("CSV header is missing or empty"));
        }

        Set<String> seen = new HashSet<>();
        for (String header : headerLine) {
            String trimmed = header.trim();
            if (trimmed.isEmpty()) {
                throw new FormatException(ErrorCode.CSV_HEADER_EMPTY, new Exception("CSV header contains empty column names"));
            }
            if (!seen.add(trimmed)) {
                throw new FormatException(ErrorCode.CSV_HEADER_DUPLICATE, new Exception("CSV header contains duplicate column: " + trimmed));
            }
            columnOrder.add(trimmed);
        }

        log.info("Extracted headers: {}", columnOrder);
    }

    private void processRowsFromCSV(CSVReader reader) {
        try {
            int lineNumber = 2;
            String[] row;
            while ((row = reader.readNext()) != null) {

                if (!validateCSCRow(row, lineNumber))
                    continue;

                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < columnOrder.size(); i++) {
                    rowMap.put(columnOrder.get(i), row[i].trim());
                }

                dataRows.add(rowMap);
                lineNumber++;
            }
        } catch (FormatException e) {
            throw e;
        } catch (Exception e) {
            throw new FormatException(ErrorCode.CSV_PARSE_ERROR, e);
        }
    }

    private boolean validateCSCRow(String[] row, int lineNumber) {
        if (row.length == 0 || Arrays.stream(row).allMatch(String::isBlank)) {
            log.warn("Skipping empty row at line {}", lineNumber);
            return false;
        }

        if (row.length != columnOrder.size()) {
            throw new FormatException(
                    ErrorCode.CSV_ROW_COLUMN_MISMATCH,
                    new Exception(String.format("CSV row at line %d has %d columns, expected %d",
                            lineNumber, row.length, columnOrder.size()))
            );
        }

        return true;
    }
}
