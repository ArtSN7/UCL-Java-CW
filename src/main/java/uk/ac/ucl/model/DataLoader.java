package uk.ac.ucl.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    public DataFrame loadCsv(Path path) {
        DataFrame dataFrame = new DataFrame();

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);

            if (content.isBlank()) {
                return dataFrame;
            }

            String[] lines = content.split("\\R", -1);
            int firstNonEmptyLineIndex = findFirstNonEmptyLineIndex(lines);
            int lastNonEmptyLineIndex = findLastNonEmptyLineIndex(lines);
            if (firstNonEmptyLineIndex < 0 || lastNonEmptyLineIndex < 0) {
                return dataFrame;
            }

            int headerLineNumber = firstNonEmptyLineIndex + 1;
            String headerLine = lines[firstNonEmptyLineIndex];
            String[] headerValues = splitCsvLine(headerLine, headerLineNumber);
            List<String> columnNames = buildColumnsFromHeader(dataFrame, headerValues, headerLineNumber);

            for (int index = firstNonEmptyLineIndex + 1; index <= lastNonEmptyLineIndex; index++) {
                String[] rowValues = parseRowValues(lines, index, columnNames.size());
                for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                    dataFrame.addValue(columnNames.get(columnIndex), rowValues[columnIndex]);
                }
            }

            return dataFrame;

        } catch (IOException exception) {
            System.err.println("Unable to read CSV file '" + path + "': " + exception.getMessage());
            return new DataFrame();
        }
    }

    private static String[] parseRowValues(String[] lines, int index, int expectedColumnCount) {
        String line = lines[index];
        int lineNumber = index + 1;

        if (line.isBlank()) {
            throw new IllegalArgumentException("Blank line found at CSV line " + lineNumber + ".");
        }

        String[] rowValues = splitCsvLine(line, lineNumber);
        if (rowValues.length != expectedColumnCount) {
            throw new IllegalArgumentException(
                    "Malformed row at line " + lineNumber + ": expected " + expectedColumnCount
                            + " values but found " + rowValues.length + ".");
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

    private static String[] splitCsvLine(String line, int lineNumber) {
        if (line.contains("\"")) {
            throw new IllegalArgumentException(
                    "Quoted fields are not supported in phase 1 (line " + lineNumber + ").");
        }
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
