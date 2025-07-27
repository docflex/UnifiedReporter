package org.unified.utils;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.unified.common.enums.ErrorCode;
import org.unified.common.enums.FileExportFormat;
import org.unified.common.exceptions.ReportException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReportExporterTest {

    private static JasperReport report;

    @BeforeAll
    static void setup() throws Exception {
        try (InputStream is = ReportExporterTest.class.getResourceAsStream("/templates/simple_report.jrxml")) {
            assertNotNull(is, "sample_report.jrxml must be in test resources");
            JasperDesign design = JRXmlLoader.load(is);
            report = JasperCompileManager.compileReport(design);
        }
    }

    private List<Map<String, Object>> sampleData() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "Alice");
        row.put("age", 30);
        return List.of(row);
    }

    private Map<String, Object> sampleParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("ReportTitle", "Test Report");
        return params;
    }

    @Test
    void exportToPdf_returnsNonEmptyByteArray() {
        byte[] bytes = ReportExporter.export(sampleData(), report, sampleParams(), FileExportFormat.PDF);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void exportToHtml_returnsNonEmptyByteArray() {
        byte[] bytes = ReportExporter.export(sampleData(), report, sampleParams(), FileExportFormat.HTML);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void exportToXml_returnsNonEmptyByteArray() {
        byte[] bytes = ReportExporter.export(sampleData(), report, sampleParams(), FileExportFormat.XML);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void exportToXlsx_returnsNonEmptyByteArray() {
        byte[] bytes = ReportExporter.export(sampleData(), report, sampleParams(), FileExportFormat.XLSX);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void export_withEmptyData_throwsException() {
        List<Map<String, Object>> emptyData = new ArrayList<>();
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportExporter.export(emptyData, report, sampleParams(), FileExportFormat.PDF));
        assertEquals(ErrorCode.REPORT_DATA_EMPTY, ex.getErrorCode());
    }

    @Test
    void export_withNullData_throwsException() {
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportExporter.export(null, report, sampleParams(), FileExportFormat.PDF));
        assertEquals(ErrorCode.REPORT_DATA_EMPTY, ex.getErrorCode());
    }

    @Test
    void exportToXlsx_whenExporterFails_throwsExportFailedException() {
        JasperPrint mockPrint = mock(JasperPrint.class);
        List<Map<String, Object>> data = sampleData();
        ReportException ex = assertThrows(ReportException.class, () -> {
            ReportExporter.exportToXlsx(mockPrint);
        });
        assertEquals(ErrorCode.REPORT_EXPORT_FAILED, ex.getErrorCode());
    }

    @Test
    void export_withUnsupportedFormat_throwsException() {
        FileExportFormat unsupported = FileExportFormat.DOCX;
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportExporter.export(sampleData(), report, sampleParams(), unsupported));
        assertEquals(ErrorCode.REPORT_FORMAT_UNSUPPORTED, ex.getErrorCode());
    }

}
