package org.unified.reporter;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.unified.common.enums.ErrorCode;
import org.unified.common.enums.FileExportFormat;
import org.unified.common.exceptions.ReportException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportCreator {

    /**
     * Generates a report from the provided data and JasperReports template, and exports it to the specified output path.
     *
     * <p>This method supports both compiled (.jasper) and uncompiled (.jrxml) Jasper templates. It fills the report with
     * the given data and parameters, and exports it in the format specified by {@code format}.</p>
     *
     * @param data                       The list of data records to populate the report, where each map represents a row.
     * @param additionalReportParameters Additional parameters to pass to the JasperReports engine (can be empty or null).
     * @param outputPath                 The file system path (as a string) where the generated report should be saved.
     * @param templateStream             The input stream of the Jasper template (.jrxml or .jasper); must not be {@code null}.
     * @param format                     The export format (e.g., PDF, HTML, XLSX); must not be {@code null}.
     * @param isCompiledTemplate         {@code true} if the template is precompiled (.jasper), {@code false} if raw (.jrxml).
     * @throws ReportException if:
     *                         <ul>
     *                             <li>The {@code templateStream} is {@code null} ({@link ErrorCode#REPORT_TEMPLATE_NULL}).</li>
     *                             <li>The {@code data} is {@code null} or empty ({@link ErrorCode#REPORT_DATA_EMPTY}).</li>
     *                             <li>The {@code format} is {@code null} or unsupported ({@link ErrorCode#REPORT_FORMAT_UNSUPPORTED}).</li>
     *                             <li>The report fails to be filled or exported ({@link ErrorCode#REPORT_FILL_FAILED} or {@link ErrorCode#REPORT_EXPORT_FAILED}).</li>
     *                         </ul>
     */
    public static void generateReport(
            List<Map<String, Object>> data,
            Map<String, Object> additionalReportParameters,
            String outputPath,
            InputStream templateStream,
            FileExportFormat format,
            boolean isCompiledTemplate // true if .jasper, false if .jrxml
    ) {
        if (templateStream == null) {
            throw new ReportException(ErrorCode.REPORT_TEMPLATE_NULL);
        }

        if (data == null || data.isEmpty()) {
            throw new ReportException(ErrorCode.REPORT_DATA_EMPTY);
        }

        try {
            JasperReport report = getJasperReportFormat(templateStream, isCompiledTemplate);
            JasperPrint print = fillReport(report, additionalReportParameters, data);

            exportReport(print, outputPath, format);
            System.out.println(format + " report generated at: " + outputPath);

        } catch (JRException e) {
            throw new ReportException(ErrorCode.REPORT_FILL_FAILED, e);
        } catch (UnsupportedOperationException e) {
            throw new ReportException(ErrorCode.REPORT_FORMAT_UNSUPPORTED, e);
        }
    }

    /**
     * Overloaded convenience method for {@link #generateReport(List, Map, String, InputStream, FileExportFormat, boolean)}
     * that accepts a {@link Path} for the output instead of a String.
     *
     * @param data                       The list of data records to populate the report.
     * @param additionalReportParameters Additional parameters to pass to the JasperReports engine.
     * @param outputPath                 The output file path as a {@link Path}; must not be {@code null}.
     * @param templateStream             The input stream of the Jasper template.
     * @param format                     The export format (PDF, HTML, XLSX).
     * @param isCompiledTemplate         {@code true} if the template is a compiled .jasper file; otherwise, {@code false}.
     * @throws ReportException if:
     *                         <ul>
     *                             <li>The {@code outputPath} is {@code null} ({@link ErrorCode#REPORT_OUTPUT_PATH_INVALID}).</li>
     *                             <li>Any other error occurs in the main generation logic (see the main method).</li>
     *                         </ul>
     */
    public static void generateReport(
            List<Map<String, Object>> data,
            Map<String, Object> additionalReportParameters,
            Path outputPath,
            InputStream templateStream,
            FileExportFormat format,
            boolean isCompiledTemplate
    ) {
        if (outputPath == null) {
            throw new ReportException(ErrorCode.REPORT_OUTPUT_PATH_INVALID);
        }
        generateReport(data, additionalReportParameters, outputPath.toString(), templateStream, format, isCompiledTemplate);
    }

    private static JasperReport getJasperReportFormat(InputStream templateStream, boolean isCompiled) throws JRException {
        try {
            return isCompiled
                    ? (JasperReport) JRLoader.loadObject(templateStream)
                    : JasperCompileManager.compileReport(templateStream);
        } catch (JRException e) {
            throw new ReportException(
                    isCompiled ? ErrorCode.REPORT_TEMPLATE_LOAD_FAILED : ErrorCode.REPORT_TEMPLATE_COMPILE_FAILED, e
            );
        }
    }

    private static JasperPrint fillReport(
            JasperReport report,
            Map<String, Object> params,
            List<Map<String, Object>> data
    ) throws JRException {
        try {
            // Defensive copy to make sure JasperReports can mutate safely
            Map<String, Object> mutableParams = new HashMap<>(params);

            @SuppressWarnings("unchecked")
            JRMapCollectionDataSource dataSource =
                    new JRMapCollectionDataSource((Collection<Map<String, ?>>) (Collection<?>) data);

            return JasperFillManager.fillReport(report, mutableParams, dataSource);

        } catch (JRException e) {
            throw new ReportException(ErrorCode.REPORT_FILL_FAILED, e);
        }
    }

    private static void exportReport(JasperPrint print, String outputPath, FileExportFormat format) throws JRException {
        if (format == null) {
            throw new ReportException(ErrorCode.REPORT_FORMAT_UNSUPPORTED);
        }
        try {
            switch (format) {
                case PDF -> JasperExportManager.exportReportToPdfFile(print, outputPath);
                case HTML -> JasperExportManager.exportReportToHtmlFile(print, outputPath);
                case XLSX -> exportToXlsx(print, outputPath);
                default -> throw new UnsupportedOperationException("Unsupported format: " + format);
            }
        } catch (JRException e) {
            throw new ReportException(ErrorCode.REPORT_EXPORT_FAILED, e);
        }
    }

    private static void exportToXlsx(JasperPrint print, String outputPath) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPath));

        SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
        config.setOnePagePerSheet(false);
        config.setDetectCellType(true);
        config.setCollapseRowSpan(false);

        exporter.setConfiguration(config);
        exporter.exportReport();
    }
}