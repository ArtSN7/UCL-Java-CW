package uk.ac.ucl.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

            List<List<String>> records = parseCsvRecords(content);
            List<List<String>> nonEmptyRecords = records.stream()
                    .filter(record -> !isBlankRecord(record))
                    .toList();

            if (nonEmptyRecords.isEmpty()) {
                LOGGER.warning(() -> "CSV file has no non-empty rows: " + path + ".");
                return dataFrame;
            }

            List<String> headerValues = nonEmptyRecords.get(0);
            List<String> columnNames = buildColumnsFromHeader(dataFrame, headerValues, 1);

            int skippedRows = 0;
            for (int index = 1; index < nonEmptyRecords.size(); index++) {
                List<String> record = nonEmptyRecords.get(index);
                int lineNumber = index + 1;

                if (record.size() != columnNames.size()) {
                    skippedRows++;
                    LOGGER.warning(() -> "Skipping malformed row at line " + lineNumber
                            + ": expected " + columnNames.size()
                            + " values but found " + record.size() + ".");
                    continue;
                }

                for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                    dataFrame.addValue(columnNames.get(columnIndex), record.get(columnIndex));
                }
            }

            if (skippedRows > 0) {
                int totalSkippedRows = skippedRows;
                LOGGER.warning(() -> "Skipped " + totalSkippedRows + " malformed row(s) in " + path + ".");
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

    public void saveCsv(Path path, DataFrame dataFrame) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("CSV path cannot be null.");
        }
        if (dataFrame == null) {
            throw new IllegalArgumentException("DataFrame cannot be null.");
        }

        Path normalizedPath = path.toAbsolutePath().normalize();
        Path parentDirectory = normalizedPath.getParent();
        if (parentDirectory == null) {
            parentDirectory = Path.of(".").toAbsolutePath().normalize();
        }
        Files.createDirectories(parentDirectory);

        Path tempFile = Files.createTempFile(parentDirectory, "patients-", ".csv.tmp");
        try {
            Files.writeString(tempFile, buildCsv(dataFrame), StandardCharsets.UTF_8);
            moveAtomically(tempFile, normalizedPath);
            LOGGER.info(() -> "Saved CSV " + normalizedPath + " with " + dataFrame.getRowCount() + " rows.");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static List<List<String>> parseCsvRecords(String content) {
        List<List<String>> records = new ArrayList<>();
        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < content.length(); index++) {
            char currentChar = content.charAt(index);

            if (currentChar == '"') {
                if (inQuotes && index + 1 < content.length() && content.charAt(index + 1) == '"') {
                    currentField.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (!inQuotes && currentChar == ',') {
                currentRecord.add(currentField.toString());
                currentField.setLength(0);
                continue;
            }

            if (!inQuotes && (currentChar == '\n' || currentChar == '\r')) {
                if (currentChar == '\r' && index + 1 < content.length() && content.charAt(index + 1) == '\n') {
                    index++;
                }
                currentRecord.add(currentField.toString());
                currentField.setLength(0);
                records.add(currentRecord);
                currentRecord = new ArrayList<>();
                continue;
            }

            currentField.append(currentChar);
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Malformed CSV: unterminated quoted field.");
        }

        if (currentField.length() > 0 || !currentRecord.isEmpty()) {
            currentRecord.add(currentField.toString());
            records.add(currentRecord);
        }

        return records;
    }

    private static boolean isBlankRecord(List<String> record) {
        for (String value : record) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static List<String> buildColumnsFromHeader(DataFrame dataFrame, List<String> headerValues, int lineNumber) {
        List<String> columnNames = new ArrayList<>(headerValues.size());

        for (int columnIndex = 0; columnIndex < headerValues.size(); columnIndex++) {
            String headerName = headerValues.get(columnIndex) == null ? "" : headerValues.get(columnIndex).trim();
            if (headerName.isEmpty()) {
                throw new IllegalArgumentException(
                        "Header name cannot be blank at line " + lineNumber + ", column " + (columnIndex + 1) + ".");
            }
            dataFrame.addColumn(new Column(headerName));
            columnNames.add(headerName);
        }

        return columnNames;
    }

    private static String buildCsv(DataFrame dataFrame) {
        List<String> columns = dataFrame.getColumnNames();
        if (columns.isEmpty()) {
            return "";
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append(renderCsvRow(columns));

        for (int row = 0; row < dataFrame.getRowCount(); row++) {
            csvContent.append(System.lineSeparator());
            csvContent.append(renderCsvRow(dataFrame.getRowValuesInColumnOrder(row)));
        }

        return csvContent.toString();
    }

    private static String renderCsvRow(List<String> values) {
        StringBuilder row = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                row.append(',');
            }
            row.append(escapeCsvValue(values.get(index)));
        }
        return row.toString();
    }

    private static String escapeCsvValue(String rawValue) {
        String value = rawValue == null ? "" : rawValue;
        boolean requiresQuoting = value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r");

        if (!requiresQuoting) {
            return value;
        }

        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
