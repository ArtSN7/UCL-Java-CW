package uk.ac.ucl.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Model {
  private static final String ID_COLUMN = "ID";
  private static final String FIRST_COLUMN = "FIRST";
  private static final String LAST_COLUMN = "LAST";
  private static final String CITY_COLUMN = "CITY";
  private static final String STATE_COLUMN = "STATE";
  private static final List<String> TABLE_COLUMNS = List.of(
    ID_COLUMN,
    FIRST_COLUMN,
    LAST_COLUMN,
    CITY_COLUMN,
    STATE_COLUMN
  );

  private final DataFrame dataFrame;
  private final Set<String> availableColumns;
  private final List<String> columnOrder;

  public Model(Path csvPath) throws IOException {
    if (csvPath == null) {
      throw new IllegalArgumentException("CSV path cannot be null.");
    }

    Path normalizedPath = csvPath.toAbsolutePath().normalize();

    if (!Files.isRegularFile(normalizedPath)) {
      throw new IOException("CSV file does not exist: " + normalizedPath);
    }
    if (!Files.isReadable(normalizedPath)) {
      throw new IOException("CSV file is not readable: " + normalizedPath);
    }

    DataLoader loader = new DataLoader();
    DataFrame loadedFrame;
    try {
      loadedFrame = loader.loadCsv(normalizedPath);
    } catch (RuntimeException exception) {
      throw new IOException("Failed to parse CSV file: " + normalizedPath, exception);
    }

    if (loadedFrame.getColumnNames().isEmpty()) {
      throw new IOException("CSV file did not load any columns: " + normalizedPath);
    }

    dataFrame = loadedFrame;
    columnOrder = loadedFrame.getColumnNames();
    availableColumns = new HashSet<>(columnOrder);

    if (!availableColumns.contains(ID_COLUMN)) {
      throw new IOException("CSV file is missing required column: " + ID_COLUMN);
    }
  }

  public List<String> getTableColumns() {
    return TABLE_COLUMNS;
  }

  public List<Map<String, String>> getPatientTableRows() {
    int rowCount = dataFrame.getRowCount();
    List<Map<String, String>> rows = new ArrayList<>(rowCount);

    for (int row = 0; row < rowCount; row++) {
      Map<String, String> rowData = new LinkedHashMap<>();
      for (String columnName : TABLE_COLUMNS) {
        rowData.put(columnName, readValue(columnName, row));
      }
      rows.add(rowData);
    }

    return rows;
  }

  public Map<String, String> getPatientDetails(String patientId) {
    if (patientId == null || patientId.isBlank()) {
      throw new IllegalArgumentException("Patient id cannot be blank.");
    }

    String normalizedId = patientId.trim();
    int row = findRowById(normalizedId);
    if (row < 0) {
      throw new NoSuchElementException("Unknown patient id: " + normalizedId);
    }

    Map<String, String> details = new LinkedHashMap<>();
    for (String columnName : columnOrder) {
      details.put(columnName, readValue(columnName, row));
    }
    return details;
  }

  private int findRowById(String patientId) {
    for (int row = 0; row < dataFrame.getRowCount(); row++) {
      if (patientId.equals(readValue(ID_COLUMN, row))) {
        return row;
      }
    }
    return -1;
  }

  private String readValue(String columnName, int row) {
    if (!availableColumns.contains(columnName)) {
      return "";
    }

    String value = dataFrame.getValue(columnName, row);
    return value == null ? "" : value;
  }
}
