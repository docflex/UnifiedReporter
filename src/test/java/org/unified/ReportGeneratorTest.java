package org.unified;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.unified.common.enums.ErrorCode;
import org.unified.common.enums.FileExportFormat;
import org.unified.common.exceptions.ReportException;
import org.unified.formats.UnifiedFormat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    static InputStream validJrxml;
    static UnifiedFormat mockFormat;

    @BeforeAll
    static void setup() {
        // Load valid JRXML from test resources
        validJrxml = ReportGeneratorTest.class.getResourceAsStream("/templates/simple_report.jrxml");
        assertNotNull(validJrxml, "JRXML template must exist in test resources");

        // Mock UnifiedFormat with data
        mockFormat = new UnifiedFormat() {
            @Override
            public List<Map<String, Object>> getDataRows() {
                Map<String, Object> row = new HashMap<>();
                row.put("name", "Alice");
                row.put("age", 30);
                return List.of(row);
            }
        };
    }

    @Test
    void generateReport_pdf_successfullyGeneratesReport() {
        byte[] result = ReportGenerator.generateReport(
                mockFormat,
                validJrxml,
                Map.of("ReportTitle", "Test"),
                FileExportFormat.PDF
        );

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateReport_withUnsupportedInputStream_throwsUnsupportedFormat() {
        InputStream dummyStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportGenerator.generateReport(
                        dummyStream,
                        validJrxml,
                        Map.of(),
                        FileExportFormat.XML
                )
        );

        assertEquals(ErrorCode.BYTE_UNSUPPORTED_FORMAT, ex.getErrorCode());
    }

    @Test
    void generateReport_withNullTemplate_throwsTemplateNull() {
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportGenerator.generateReport(
                        mockFormat,
                        null,
                        Map.of(),
                        FileExportFormat.XLSX
                )
        );

        assertEquals(ErrorCode.REPORT_TEMPLATE_NULL, ex.getErrorCode());
    }

    @Test
    void generateReport_withInvalidTemplate_throwsTemplateLoadFailed() {
        // Create a bogus stream (not .jrxml or .jasper)
        InputStream invalidStream = new ByteArrayInputStream("not a jasper file".getBytes());

        ReportException ex = assertThrows(ReportException.class, () ->
                ReportGenerator.generateReport(
                        mockFormat,
                        invalidStream,
                        Map.of(),
                        FileExportFormat.PDF
                )
        );

        assertEquals(ErrorCode.REPORT_TEMPLATE_COMPILE_FAILED, ex.getErrorCode());
    }

    @Test
    void generateReport_exportFails_throwsReportExportFailed() {
        UnifiedFormat emptyFormat = new UnifiedFormat() {
            @Override
            public List<Map<String, Object>> getDataRows() {
                return Collections.emptyList(); // triggers export failure
            }
        };

        ReportException ex = assertThrows(ReportException.class, () ->
                ReportGenerator.generateReport(
                        emptyFormat,
                        ReportGeneratorTest.class.getResourceAsStream("/templates/simple_report.jrxml"),
                        Map.of(),
                        FileExportFormat.PDF
                )
        );

        assertEquals(ErrorCode.REPORT_DATA_EMPTY, ex.getErrorCode());
    }

    @Test
    void generateReport_withUnexpectedError_throwsRuntimeException() {
        // Use a UnifiedFormat that throws inside getDataRows()
        UnifiedFormat brokenFormat = new UnifiedFormat() {
            @Override
            public List<Map<String, Object>> getDataRows() {
                throw new NullPointerException("Boom");
            }
        };

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                ReportGenerator.generateReport(
                        brokenFormat,
                        validJrxml,
                        Map.of(),
                        FileExportFormat.HTML
                )
        );

        assertInstanceOf(ReportException.class, ex.getCause());
    }
}
