package uk.ac.ucl.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataLoader {
    private static final Logger LOGGER = Logger.getLogger(DataLoader.class.getName());

    public DataFrame loadCsv(Path path) {
        DataFrame dataFrame = new DataFrame();
        LOGGER.fine(() -> "Loading CSV from " + path + ".");

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);

            if (content.isBlank()) {
                LOGGER.warning(() -> "CSV file is blank: " + path + ".");
                return dataFrame;
            }

            String[] lines = content.split("\\R", -1);
            int firstNonEmptyLineIndex = findFirstNonEmptyLineIndex(lines);
            int lastNonEmptyLineIndex = findLastNonEmptyLineIndex(lines);
            if (firstNonEmptyLineIndex < 0 || lastNonEmptyLineIndex < 0) {
                LOGGER.warning(() -> "CSV file has no non-empty lines: " + path + ".");
                return dataFrame;
            }

            String headerLine = lines[firstNonEmptyLineIndex];
            String[] headerValues = splitCsvLine(headerLine);
            List<String> columnNames = buildColumnsFromHeader(dataFrame, headerValues, firstNonEmptyLineIndex + 1);

            int skippedRows = 0;
            for (int index = firstNonEmptyLineIndex + 1; index <= lastNonEmptyLineIndex; index++) {
                String[] rowValues = parseRowValues(lines, index, columnNames.size());
                if (rowValues == null) {
                    skippedRows++;
                    continue;
                }
                for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                    dataFrame.addValue(columnNames.get(columnIndex), rowValues[columnIndex]);
                }
            }
            if (skippedRows > 0) {
                int totalSkippedRows = skippedRows;
                LOGGER.warning(() -> "Skipped " + totalSkippedRows + " malformed/blank row(s) in " + path + ".");
            }

            LOGGER.info(() -> "Loaded CSV " + path + " with "
                    + dataFrame.getRowCount() + " rows and "
                    + dataFrame.getColumnNames().size() + " columns.");
            return dataFrame;

        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Unable to read CSV file '" + path + "'.", exception);
            return new DataFrame();
        }
    }

    private static String[] parseRowValues(String[] lines, int index, int expectedColumnCount) {
        String line = lines[index];
        int lineNumber = index + 1;

        if (line.isBlank()) {
            LOGGER.warning(() -> "Skipping blank line at CSV line " + lineNumber + ".");
            return null;
        }

        String[] rowValues = splitCsvLine(line);
        if (rowValues.length != expectedColumnCount) {
            LOGGER.warning(() -> "Skipping malformed row at line " + lineNumber
                    + ": expected " + expectedColumnCount
                    + " values but found " + rowValues.length + ".");
            return null;
        }
        return rowValues;
    }

    private static List<String> buildColumnsFromHeader(
            DataFrame dataFrame,
            String[] headerValues,
            int lineNumber) {
        List<String> columnNames = new ArrayList<>(headerValues.length);
        for (int columnIndex = 0; columnIndex < headerValues.length; columnIndex++) {
            String headerValue = headerValues[columnIndex];
            String headerName = headerValue.trim();

            if (headerName.isEmpty()) {
                throw new IllegalArgumentException(
                        "Header name cannot be blank at line " + lineNumber + ", column " + (columnIndex + 1) + ".");
            }

            dataFrame.addColumn(new Column(headerName));
            columnNames.add(headerName);
        }
        return columnNames;
    }

    private static String[] splitCsvLine(String line) {
        return line.split(",", -1);
    }

    private static int findFirstNonEmptyLineIndex(String[] lines) {
        for (int index = 0; index < lines.length; index++) {
            if (!lines[index].isBlank()) {
                return index;
            }
        }
        return -1;
    }

    private static int findLastNonEmptyLineIndex(String[] lines) {
        for (int index = lines.length - 1; index >= 0; index--) {
            if (!lines[index].isBlank()) {
                return index;
            }
        }
        return -1;
    }
}
