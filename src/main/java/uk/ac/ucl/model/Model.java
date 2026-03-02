package uk.ac.ucl.model;

import uk.ac.ucl.config.AppConstants;

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
import java.util.logging.Logger;

public class Model {
  private static final Logger LOGGER = Logger.getLogger(Model.class.getName());

  private final DataFrame dataFrame;
  private final Set<String> availableColumns;
  private final List<String> columnOrder;

  public Model(Path csvPath) throws IOException {
    if (csvPath == null) {
      LOGGER.warning("Attempted to create Model with null CSV path.");
      throw new IllegalArgumentException("CSV path cannot be null.");
    }

    Path normalizedPath = csvPath.toAbsolutePath().normalize();

    if (!Files.isRegularFile(normalizedPath)) {
      LOGGER.warning(() -> "CSV file does not exist: " + normalizedPath + ".");
      throw new IOException("CSV file does not exist: " + normalizedPath);
    }
    if (!Files.isReadable(normalizedPath)) {
      LOGGER.warning(() -> "CSV file is not readable: " + normalizedPath + ".");
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

    if (!availableColumns.contains(AppConstants.CsvColumns.ID)) {
      LOGGER.warning(() -> "CSV is missing required ID column: " + normalizedPath + ".");
      throw new IOException("CSV file is missing required column: " + AppConstants.CsvColumns.ID);
    }
    LOGGER.info(() -> "Model loaded from " + normalizedPath + " with "
      + dataFrame.getRowCount() + " rows.");
  }

  public List<String> getTableColumns() {
    return AppConstants.CsvColumns.TABLE_COLUMNS;
  }

  public List<Map<String, String>> getPatientTableRows() {
    int rowCount = dataFrame.getRowCount();
    List<Map<String, String>> rows = new ArrayList<>(rowCount);

    for (int row = 0; row < rowCount; row++) {
      Map<String, String> rowData = new LinkedHashMap<>();
      for (String columnName : AppConstants.CsvColumns.TABLE_COLUMNS) {
        rowData.put(columnName, readValue(columnName, row));
      }
      rows.add(rowData);
    }

    return rows;
  }

  public Map<String, String> getPatientDetails(String patientId) {
    if (patientId == null || patientId.isBlank()) {
      LOGGER.warning("Patient detail request had a blank id.");
      throw new IllegalArgumentException("Patient id cannot be blank.");
    }

    String normalizedId = patientId.trim();
    int row = findRowById(normalizedId);
    if (row < 0) {
      LOGGER.warning(() -> "Patient id not found: " + normalizedId + ".");
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
      if (patientId.equals(readValue(AppConstants.CsvColumns.ID, row))) {
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
