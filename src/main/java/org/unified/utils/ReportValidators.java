package org.unified.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.ReportException;
import org.unified.formats.UnifiedFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Utility class that provides validations for Jasper report templates and UnifiedFormat inputs.
 * <p>
 * Supports validation and loading of compiled JasperReports (.jasper) or compiling raw JRXML streams.
 * Also ensures that input objects conform to expected formats before proceeding with reporting operations.
 */
@Slf4j
public class ReportValidators {

    /**
     * Validates and loads a JasperReport from the provided input stream.
     * <p>
     * Supports both compiled (.jasper) and source (.jrxml) templates.
     *
     * @param jasperReportTemplateStream the input stream of the report template
     * @return a valid {@link JasperReport} instance
     * @throws ReportException if the template is null, unreadable, or fails to load/compile
     */
    public static JasperReport validateJasperReport(InputStream jasperReportTemplateStream) {
        if (jasperReportTemplateStream == null) {
            throw new ReportException(ErrorCode.REPORT_TEMPLATE_NULL);
        }

        try {
            byte[] templateBytes = jasperReportTemplateStream.readAllBytes();
            Optional<JasperReport> reportTemplate = ReportValidators.loadTemplate(templateBytes);

            if (reportTemplate.isPresent()) {
                log.info("✅ Valid Jasper template loaded: {}", reportTemplate.get().getName());
                return reportTemplate.get();
            } else {
                throw new ReportException(ErrorCode.REPORT_TEMPLATE_LOAD_FAILED);
            }

        } catch (IOException e) {
            throw new ReportException(ErrorCode.IO_EXCEPTION, e);
        } catch (Exception e) {
            throw new ReportException(ErrorCode.REPORT_TEMPLATE_COMPILE_FAILED, e);
        }
    }

    /**
     * Attempts to load a JasperReport from the given byte array.
     * Tries first to deserialize a compiled .jasper file, and falls back to compiling a .jrxml source.
     *
     * @param data byte array of the Jasper template file
     * @return Optional containing a valid {@link JasperReport}, or empty if loading/compilation failed
     */
    private static Optional<JasperReport> loadTemplate(byte[] data) {
        try (InputStream is = new ByteArrayInputStream(data)) {
            Object obj = JRLoader.loadObject(is);
            if (obj instanceof JasperReport report) {
                return Optional.of(report);
            }
        } catch (IOException | JRException ignored) {
        }

        try (InputStream is = new ByteArrayInputStream(data)) {
            return Optional.of(JasperCompileManager.compileReport(is));
        } catch (IOException | JRException ignored) {
        }

        return Optional.empty();
    }

    /**
     * Validates the provided input object for use in report generation.
     * <p>
     * Accepts only instances of {@link UnifiedFormat}. Rejects InputStream or byte[] inputs
     * explicitly to prevent misuse.
     *
     * @param input the input object to validate
     * @return the input cast to {@link UnifiedFormat} if valid
     * @throws ReportException if input type is invalid or unsupported
     */
    public static UnifiedFormat validateInputFile(Object input) {
        if (input instanceof UnifiedFormat) {
            log.info("✅ Valid UnifiedFormat input received.");
            return (UnifiedFormat) input;
        } else if (input instanceof InputStream || input instanceof byte[]) {
            throw new ReportException(ErrorCode.BYTE_UNSUPPORTED_FORMAT);
        } else {
            throw new ReportException(ErrorCode.UNKNOWN_ERROR, new IllegalArgumentException("Unrecognized input type"));
        }
    }
}
