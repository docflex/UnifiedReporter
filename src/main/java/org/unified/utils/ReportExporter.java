package org.unified.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.unified.common.enums.ErrorCode;
import org.unified.common.enums.FileExportFormat;
import org.unified.common.exceptions.ReportException;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class responsible for exporting reports using JasperReports into multiple formats.
 * <p>
 * Supports export formats including PDF, HTML, XML, and XLSX.
 */
@Slf4j
public class ReportExporter {

    /**
     * Exports the provided data and compiled JasperReport template to the specified format.
     *
     * @param dataRows       the collection of data maps used as the data source for the report
     * @param reportTemplate the compiled JasperReport (.jasper)
     * @param parameters     the map of report parameters
     * @param format         the output format (PDF, HTML, XML, XLSX)
     * @return a byte array representing the exported report content
     * @throws ReportException if any step of the export process fails
     */
    public static byte[] export(Collection<Map<String, Object>> dataRows, JasperReport reportTemplate, Map<String, Object> parameters, FileExportFormat format) {
        if (dataRows == null || dataRows.isEmpty()) {
            throw new ReportException(ErrorCode.REPORT_DATA_EMPTY);
        }
        try {
            // Defensive copy of parameters
            Map<String, Object> mutableParams = new HashMap<>(parameters);

            JasperPrint jasperPrint;
            try {
                @SuppressWarnings("unchecked")
                JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(
                        (Collection<Map<String, ?>>) (Collection<?>) dataRows
                );

                jasperPrint = JasperFillManager.fillReport(reportTemplate, mutableParams, dataSource);
            } catch (JRException e) {
                log.error("❌ Failed to fill report with data", e);
                throw new ReportException(ErrorCode.REPORT_FILL_FAILED, e);
            }


            return switch (format) {
                case PDF -> exportToPdf(jasperPrint);
                case HTML -> exportToHtml(jasperPrint);
                case XML -> exportToXml(jasperPrint);
                case XLSX -> exportToXlsx(jasperPrint);
                default -> throw new ReportException(ErrorCode.REPORT_FORMAT_UNSUPPORTED);
            };

        } catch (ReportException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unknown error during report export", e);
            throw new ReportException(ErrorCode.UNKNOWN_ERROR, e);
        }
    }

    /**
     * Exports the report to PDF format.
     *
     * @param jasperPrint the filled JasperPrint object
     * @return the exported PDF as a byte array
     */
    static byte[] exportToPdf(JasperPrint jasperPrint) {
        try {
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            throw new ReportException(ErrorCode.REPORT_EXPORT_FAILED, e);
        }
    }

    /**
     * Exports the report to HTML format.
     *
     * @param jasperPrint the filled JasperPrint object
     * @return the exported HTML as a byte array
     */
    static byte[] exportToHtml(JasperPrint jasperPrint) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
            exporter.exportReport();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ReportException(ErrorCode.REPORT_EXPORT_FAILED, e);
        }
    }

    /**
     * Exports the report to XML format.
     *
     * @param jasperPrint the filled JasperPrint object
     * @return the exported XML as a byte array
     */
    static byte[] exportToXml(JasperPrint jasperPrint) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JRXmlExporter exporter = new JRXmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleXmlExporterOutput(outputStream));
            exporter.exportReport();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ReportException(ErrorCode.REPORT_EXPORT_FAILED, e);
        }
    }

    /**
     * Exports the report to XLSX (Excel) format.
     *
     * @param jasperPrint the filled JasperPrint object
     * @return the exported XLSX file as a byte array
     */
    static byte[] exportToXlsx(JasperPrint jasperPrint) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JRXlsxExporter exporter = new JRXlsxExporter();

            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setOnePagePerSheet(false);
            config.setDetectCellType(true);
            config.setCollapseRowSpan(false);
            config.setWhitePageBackground(false);
            exporter.setConfiguration(config);

            exporter.exportReport();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ReportException(ErrorCode.REPORT_EXPORT_FAILED, e);
        }
    }
}