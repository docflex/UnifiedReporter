package org.unified.formats;

import org.unified.common.exceptions.FormatException;

import java.util.List;
import java.util.Map;

/**
 * Represents a unified abstraction for structured tabular input data, such as CSV, Excel, JSON, or byte-based sources.
 * <p>
 * Implementations of this interface should parse and expose the input data as a list of rows,
 * where each row is a map of column names to their corresponding values.
 * This normalized format allows the data to be used consistently across various output engines
 * (e.g., PDF generation via JasperReports).
 */
public interface UnifiedFormat {
    /**
     * Returns the list of structured data rows extracted from the source input.
     * <p>
     * Each entry in the list represents a single row, where the map's keys are column names
     * and the values are the corresponding cell values. All rows are expected to have a consistent schema.
     *
     * @return a list of rows, each represented as a map of column name to value;
     * never {@code null}, but may be empty if no data is present.
     */
    List<Map<String, Object>> getDataRows();

    /**
     * Performs semantic or structural validation on the parsed data.
     * <p>
     * Implementations may check for required fields, data types, or
     * domain-specific rules. If validation fails, a {@link FormatException}
     * or a custom exception should be thrown.
     *
     * @throws FormatException if validation fails due to missing or invalid data.
     */
    default void validateFields() {
        // Default: no-op. Implementations can override as needed.
    }

    /**
     * Returns the explicit column order, if available, to maintain the formatting
     * or display order of columns during output rendering.
     * <p>
     * This is particularly useful when the input format preserves column positioning
     * (e.g., CSV or Excel), and the output renderer (like Jasper) should reflect
     * that ordering instead of relying on the natural key order of a map.
     *
     * @return a list of column names in their intended display order,
     * or {@code null} if no specific order is defined.
     */
    default List<String> getColumnOrder() {
        return null;
    }

    /**
     * Returns a logical name or label for the original data source.
     * <p>
     * This may refer to the file name (e.g., "employees.csv") or any other meaningful
     * identifier of the input. It can be used for metadata tagging, debugging,
     * logging, or even inclusion in the rendered output (e.g., report headers).
     *
     * @return a non-null string representing the source name; defaults to {@code "unnamed"}.
     */
    default String getSourceName() {
        return "unnamed";
    }
}
