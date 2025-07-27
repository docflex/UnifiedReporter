package org.unified;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperReport;
import org.unified.common.enums.FileExportFormat;
import org.unified.common.exceptions.ReportException;
import org.unified.formats.UnifiedFormat;
import org.unified.utils.ReportExporter;
import org.unified.utils.ReportValidators;

import java.io.InputStream;
import java.util.Map;

/**
 * Central class responsible for generating reports from structured data and Jasper templates.
 * <p>
 * This utility orchestrates the validation of input data, compilation/loading of report templates,
 * and final export of reports into multiple formats (PDF, XLSX, HTML, XML).
 * <p>
 * It abstracts the complexity behind parsing data, compiling templates, and exporting formats,
 * providing a single entry point for report generation.
 *
 * <h2>Supported Input</h2>
 * This class currently expects a single data source:
 * <ul>
 *     <li>{@link UnifiedFormat} object - a normalized wrapper over parsed tabular input (e.g., XLSX, CSV)</li>
 * </ul>
 * Other input types like {@code InputStream} or {@code byte[]} are not yet supported for dynamic format inference.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * InputStream templateStream = getClass().getResourceAsStream("/invoice_template.jasper");
 * Map<String, Object> parameters = Map.of("CompanyName", "ACME Corp.");
 *
 * UnifiedFormat file = new XLSXFormat(myExcelStream, "InvoiceData");
 *
 * byte[] pdfBytes = ReportGenerator.generateReport(
 *     file,
 *     templateStream,
 *     parameters,
 *     FileExportFormat.PDF
 * );
 *
 * Files.write(Paths.get("invoice.pdf"), pdfBytes);
 * }</pre>
 *
 * <h2>Exceptions</h2>
 * This method throws {@link ReportException} for known validation or export issues,
 * and wraps unexpected errors in a {@link RuntimeException}.
 *
 * @author Unified
 */
@Slf4j
public class ReportGenerator {

    /**
     * Generates a Jasper report from structured tabular input using the specified template and export format.
     * <p>
     * This method follows a 3-step process:
     * <ol>
     *     <li>Validates and extracts data from the input file</li>
     *     <li>Validates and compiles (or loads) the Jasper template</li>
     *     <li>Exports the filled report into the requested output format</li>
     * </ol>
     *
     * @param file                       A valid {@link UnifiedFormat} instance (e.g., XLSXFormat)
     * @param jasperReportTemplateStream Input stream of the Jasper template (.jasper or .jrxml)
     * @param additionalReportParameters Additional parameters for the Jasper report (e.g., metadata, dynamic values)
     * @param exportFormat               Desired file export format (PDF, XLSX, HTML, XML)
     * @return A byte array representing the generated report file
     * @throws ReportException  if the input is invalid, template fails to load, or export fails
     * @throws RuntimeException if an unexpected error occurs during generation
     */
    public static byte[] generateReport(
            Object file,
            InputStream jasperReportTemplateStream,
            Map<String, Object> additionalReportParameters,
            FileExportFormat exportFormat
    ) {
        long startTime = System.nanoTime();

        try {
            UnifiedFormat inputFile = ReportValidators.validateInputFile(file);

            JasperReport reportTemplate = ReportValidators.validateJasperReport(jasperReportTemplateStream);

            byte[] output = ReportExporter.export(
                    inputFile.getDataRows(),
                    reportTemplate,
                    additionalReportParameters,
                    exportFormat
            );

            long endTime = System.nanoTime();
            long durationMillis = (endTime - startTime) / 1_000_000;

            log.info("✅ Report generated successfully in {} ms (Format: {})", durationMillis, exportFormat);
            return output;

        } catch (ReportException rex) {
            throw rex;
        } catch (Exception e) {
            log.error("❌ Unexpected error during report generation", e);
            throw new RuntimeException("Report generation failed", e);
        }
    }
}
