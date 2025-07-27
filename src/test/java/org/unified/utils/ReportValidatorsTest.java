package org.unified.utils;

import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.ReportException;
import org.unified.formats.UnifiedFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportValidatorsTest {

    @Test
    void validateJasperReport_withNullInput_throwsTemplateNullException() {
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportValidators.validateJasperReport(null));
        assertEquals(ErrorCode.REPORT_TEMPLATE_NULL, ex.getErrorCode());
    }

    @Test
    void validateJasperReport_withInvalidBytes_throwsLoadFailedException() {
        byte[] invalidData = "not-a-valid-template".getBytes();
        InputStream stream = new ByteArrayInputStream(invalidData);

        ReportException ex = assertThrows(ReportException.class, () ->
                ReportValidators.validateJasperReport(stream));
        assertEquals(ErrorCode.REPORT_TEMPLATE_COMPILE_FAILED, ex.getErrorCode());
    }

    @Test
    void validateJasperReport_withValidJrxml_compilesSuccessfully() throws Exception {
        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jrxml")) {
            assertNotNull(jrxml, "JRXML template must be available in test resources");
            JasperReport report = ReportValidators.validateJasperReport(jrxml);
            assertNotNull(report);
            assertNotNull(report.getName());
        }
    }

    @Test
    void validateJasperReport_withValidJasper_compilesSuccessfully() throws Exception {
        try (InputStream jrxml = getClass().getResourceAsStream("/templates/simple_report.jasper")) {
            assertNotNull(jrxml, "Jasper template must be available in test resources");
            JasperReport report = ReportValidators.validateJasperReport(jrxml);
            assertNotNull(report);
            assertNotNull(report.getName());
        }
    }

    @Test
    void validateInputFile_withValidUnifiedFormat_passesValidation() {
        UnifiedFormat input = new UnifiedFormat() {
            @Override
            public List<Map<String, Object>> getDataRows() {
                return List.of();
            }
            // implement interface or use mockito if available
        };

        UnifiedFormat validated = ReportValidators.validateInputFile(input);
        assertSame(input, validated);
    }

    @Test
    void validateInputFile_withInputStream_throwsUnsupportedFormat() {
        InputStream dummyStream = new ByteArrayInputStream(new byte[]{});
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportValidators.validateInputFile(dummyStream));
        assertEquals(ErrorCode.BYTE_UNSUPPORTED_FORMAT, ex.getErrorCode());
    }

    @Test
    void validateInputFile_withByteArray_throwsUnsupportedFormat() {
        byte[] data = new byte[]{1, 2, 3};
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportValidators.validateInputFile(data));
        assertEquals(ErrorCode.BYTE_UNSUPPORTED_FORMAT, ex.getErrorCode());
    }

    @Test
    void validateInputFile_withUnsupportedType_throwsUnknownError() {
        Object randomInput = 12345; // e.g., Integer
        ReportException ex = assertThrows(ReportException.class, () ->
                ReportValidators.validateInputFile(randomInput));
        assertEquals(ErrorCode.UNKNOWN_ERROR, ex.getErrorCode());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void validateJasperReport_whenIOExceptionThrown_throwsIoExceptionErrorCode() {
        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read failure");
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                throw new IOException("Simulated readAllBytes failure");
            }
        };

        ReportException ex = assertThrows(ReportException.class, () ->
                ReportValidators.validateJasperReport(brokenStream));

        assertEquals(ErrorCode.IO_EXCEPTION, ex.getErrorCode());
        assertTrue(ex.getCause() instanceof IOException);
    }
}
