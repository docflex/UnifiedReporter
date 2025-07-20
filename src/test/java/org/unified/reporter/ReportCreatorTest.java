package org.unified.reporter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.unified.common.enums.ErrorCode;
import org.unified.common.enums.FileExportFormat;
import org.unified.common.exceptions.ReportException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportCreatorTest {

    private List<Map<String, Object>> sampleData;
    private Map<String, Object> params;
    private Path outputDir;

    @BeforeAll
    void setup() throws IOException {
        sampleData = List.of(
                Map.of("name", "Alice", "score", 85),
                Map.of("name", "Bob", "score", 92)
        );
        params = Map.of("ReportTitle", "Test Report");

        outputDir = Files.createTempDirectory("report-tests");
    }

    @AfterAll
    void cleanup() throws IOException {
        Files.walk(outputDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
    }

    @Test
    void shouldGeneratePdfFromJrxmlTemplate() throws Exception {
        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jrxml")) {
            Path output = outputDir.resolve("report.pdf");

            ReportCreator.generateReport(sampleData, params, output, jrxml, FileExportFormat.PDF, false);

            assertTrue(Files.exists(output), "PDF report should be generated");
        }
    }

    @Test
    void shouldGenerateXlsxFromCompiledTemplate() throws Exception {
        try (InputStream jasper = getClass().getResourceAsStream("/templates/simple_report.jasper")) {
            Path output = outputDir.resolve("report.xlsx");

            ReportCreator.generateReport(sampleData, params, output, jasper, FileExportFormat.XLSX, true);

            assertTrue(Files.exists(output), "XLSX report should be generated");
        }
    }

    @Test
    void shouldThrowIfTemplateStreamIsNull() {
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportCreator.generateReport(sampleData, params, "invalid", null, FileExportFormat.PDF, false)
        );
        assertEquals(ErrorCode.REPORT_TEMPLATE_NULL, ex.getErrorCode());
    }

    @Test
    void shouldThrowIfDataIsEmpty() {
        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jrxml")) {
            ReportException ex = assertThrows(ReportException.class, () ->
                    ReportCreator.generateReport(List.of(), params, "invalid", jrxml, FileExportFormat.PDF, false)
            );
            assertEquals(ErrorCode.REPORT_DATA_EMPTY, ex.getErrorCode());
        } catch (IOException e) {
            fail("Template loading failed");
        }
    }

    @Test
    void shouldThrowForUnsupportedFormat() {
        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jrxml")) {
            ReportException ex = assertThrows(ReportException.class, () ->
                    ReportCreator.generateReport(sampleData, params, "invalid", jrxml, null, false)
            );
            assertEquals(ErrorCode.REPORT_FORMAT_UNSUPPORTED, ex.getErrorCode());
        } catch (IOException e) {
            fail("Template loading failed");
        }
    }

    @Test
    void shouldThrowIfOutputPathIsNull() {
        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jrxml")) {
            ReportException ex = assertThrows(ReportException.class, () ->
                    ReportCreator.generateReport(sampleData, params, (Path) null, jrxml, FileExportFormat.PDF, false)
            );
            assertEquals(ErrorCode.REPORT_OUTPUT_PATH_INVALID, ex.getErrorCode());
        } catch (IOException e) {
            fail("Template loading failed");
        }
    }

//    @Test
//    void compileJrxmlTemplateToJasper() throws Exception {
//        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jrxml")) {
//            JasperReport report = JasperCompileManager.compileReport(jrxml);
//
//            // Save the compiled file
//            JasperCompileManager.compileReportToFile(
//                    getClass().getResource("/templates/simple_report.jrxml").getPath(),
//                    "src/test/resources/templates/simple_report.jasper"
//            );
//
//            assertNotNull(report);
//        }
//    }
}
