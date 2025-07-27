package org.unified.formats;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A parser class that implements {@link UnifiedFormat} for handling CSV files.
 * <p>
 * It extracts header information and rows into a list of maps,
 * while enforcing header uniqueness and row-column alignment.
 * <p>
 * UTF-8 encoding is enforced with BOM stripping to handle multi-platform CSV files.
 */
@Slf4j
public class CSVFormat implements UnifiedFormat {

    private final List<Map<String, Object>> dataRows = new ArrayList<>();
    private final List<String> columnOrder = new ArrayList<>();
    private final String sourceName;

    /**
     * Constructs a new CSVFormat parser instance from an {@link InputStream}.
     *
     * @param csvStream  the input stream containing the CSV content
     * @param sourceName the name of the CSV source, used in logs; defaults to "CSV" if null
     */
    public CSVFormat(InputStream csvStream, String sourceName) {
        this.sourceName = sourceName != null ? sourceName : "CSV";
        parse(csvStream);
    }

    /**
     * Returns the list of parsed data rows from the CSV file.
     * Each row is represented as a {@link Map} with header names as keys.
     *
     * @return list of row data as maps
     */
    @Override
    public List<Map<String, Object>> getDataRows() {
        return dataRows;
    }

    /**
     * Returns the ordered list of column headers as defined in the CSV header line.
     *
     * @return list of column headers
     */
    @Override
    public List<String> getColumnOrder() {
        return columnOrder;
    }

    /**
     * Returns the logical name of the CSV source file.
     *
     * @return the source name
     */
    @Override
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Parses the CSV input stream, extracting headers and row data.
     * Validates the headers and each row during parsing.
     *
     * @param inputStream the input stream to parse
     * @throws FormatException if parsing fails due to IO issues or malformed structure
     */
    private void parse(InputStream inputStream) {
        log.info("Starting Parsing CSV ---> UnifiedFormat");
        try (
                BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(inputStream).get();
                InputStreamReader reader = new InputStreamReader(bomInputStream, StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(reader)
        ) {
            String[] headerLine = csvReader.readNext();

            extractHeadersFromCSV(headerLine);
            processRowsFromCSV(csvReader);

        } catch (IOException e) {
            throw new FormatException(ErrorCode.CSV_IO_ERROR, e);
        } catch (FormatException e) {
            log.error("❌ Format error while parsing CSV", e);
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error while parsing CSV", e);
            throw new FormatException(ErrorCode.CSV_PARSE_ERROR, e);
        }
    }

    // Utility Functions

    /**
     * Extracts and validates the header line from the CSV.
     * Ensures headers are not empty or duplicated.
     *
     * @param headerLine the first line of the CSV, expected to contain headers
     * @throws FormatException if headers are missing, empty, or duplicated
     */
    private void extractHeadersFromCSV(String[] headerLine) {
        if (headerLine == null || headerLine.length == 0) {
            log.error("❌ CSV header is missing or empty");
            throw new FormatException(ErrorCode.CSV_HEADER_MISSING);
        }

        Set<String> seen = new HashSet<>();
        for (String header : headerLine) {
            String trimmed = header.trim();
            if (trimmed.isEmpty()) {
                log.error("❌ CSV header contains an empty column");
                throw new FormatException(ErrorCode.CSV_HEADER_EMPTY);
            }
            if (!seen.add(trimmed)) {
                log.error("❌ Duplicate header found: {}", trimmed);
                throw new FormatException(ErrorCode.CSV_HEADER_DUPLICATE);
            }
            columnOrder.add(trimmed);
        }

        log.info("Extracted headers: {}", columnOrder);
    }

    /**
     * Reads and processes all data rows in the CSV, mapping them to the corresponding headers.
     *
     * @param reader the CSVReader positioned after the header
     * @throws FormatException if any row has a mismatch in column count or processing fails
     */
    private void processRowsFromCSV(CSVReader reader) {
        try {
            int lineNumber = 2;
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (!validateCSVRow(row, lineNumber))
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
            log.error("❌ Error processing CSV rows", e);
            throw new FormatException(ErrorCode.CSV_PARSE_ERROR, e);
        }
    }

    /**
     * Validates a CSV row:
     * - Skips if empty or blank.
     * - Throws if column count doesn't match the header size.
     *
     * @param row        the row values
     * @param lineNumber the current line number (used in logs)
     * @return {@code true} if the row is valid, {@code false} if empty and should be skipped
     * @throws FormatException for column count mismatches
     */
    private boolean validateCSVRow(String[] row, int lineNumber) {
        if (row.length == 0 || Arrays.stream(row).allMatch(String::isBlank)) {
            log.warn("⚠️ Skipping empty row at line {}", lineNumber);
            return false;
        }

        if (row.length != columnOrder.size()) {
            String msg = String.format("CSV row at line %d has %d columns, expected %d", lineNumber, row.length, columnOrder.size());
            log.error("❌ Row-column mismatch: {}", msg);
            throw new FormatException(ErrorCode.CSV_ROW_COLUMN_MISMATCH);
        }

        return true;
    }
}
