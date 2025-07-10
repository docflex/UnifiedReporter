package org.unified;

import org.unified.common.enums.ErrorCode;
import org.unified.common.exceptions.FormatException;
import org.unified.formats.XLSXFormat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class UnifiedReporter {
    public static void main(String[] args) {
        try (InputStream is = new FileInputStream("/Users/docflex/Downloads/Book1.xlsx")) {
            XLSXFormat xlsxInput = new XLSXFormat(is, "/Users/docflex/Downloads/Book1.xlsx");
            List<Map<String, Object>> data = xlsxInput.getDataRows();

            data.forEach(System.out::println); // view rows
        } catch (IOException e) {
            throw new FormatException(ErrorCode.IO_EXCEPTION, e);
        }

    }
}