package uk.ac.ucl.model;

import uk.ac.ucl.config.AppConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Locale;
import java.util.logging.Logger;

public class Model {
  private static final Logger LOGGER = Logger.getLogger(Model.class.getName());
  private static Model instance;

  private final DataFrame dataFrame;
  private final List<String> tableColumns;

  private Model(Path csvPath) throws IOException {
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
    if (!loadedFrame.hasColumn(AppConstants.CsvColumns.ID)) {
      throw new IOException("CSV file is missing required column: " + AppConstants.CsvColumns.ID);
    }

    this.dataFrame = loadedFrame;
    this.tableColumns = loadedFrame.normalizeSelectedColumns(AppConstants.CsvColumns.TABLE_COLUMNS);
    LOGGER.info(() -> "Model loaded from " + normalizedPath + " with "
      + loadedFrame.getRowCount() + " rows.");
  }

  public static synchronized Model getInstance() throws IOException {
    if (instance == null) {
      Path csvPath = resolveCsvPath();
      LOGGER.info(() -> "Creating model using CSV file: " + csvPath + ".");
      instance = new Model(csvPath);
    } else {
      LOGGER.fine("Reusing existing Model singleton.");
    }
    return instance;
  }

  public List<String> getTableColumns() {
    return tableColumns;
  }

  public List<Map<String, String>> getPatientTableRows() {
    return dataFrame.getRowsForColumns(tableColumns);
  }

  public Map<String, String> getPatientDetails(String patientId) {
    if (patientId == null || patientId.isBlank()) {
      LOGGER.warning("Patient detail request had a blank id.");
      throw new IllegalArgumentException("Patient id cannot be blank.");
    }

    String normalizedId = patientId.trim();
    int row = dataFrame.findRowByValue(AppConstants.CsvColumns.ID, normalizedId);
    if (row < 0) {
      LOGGER.warning(() -> "Patient id not found: " + normalizedId + ".");
      throw new NoSuchElementException("Unknown patient id: " + normalizedId);
    }

    return dataFrame.getRecordDetails(row);
  }

  public List<Map<String, String>> searchPatients(String searchString) {
    if (searchString == null || searchString.isBlank()) {
      LOGGER.warning("Search request had a blank query.");
      throw new IllegalArgumentException("Search string cannot be blank.");
    }

    List<String> keywords = Arrays.stream(searchString.trim().toLowerCase(Locale.ROOT).split("\\s+"))
      .filter(token -> !token.isBlank())
      .toList();
    if (keywords.isEmpty()) {
      LOGGER.warning("Search request produced no valid keywords.");
      throw new IllegalArgumentException("Search string cannot be blank.");
    }

    List<Map<String, String>> matches = new ArrayList<>();
    for (int row = 0; row < dataFrame.getRowCount(); row++) {
      Map<String, String> record = dataFrame.getRecordDetails(row);
      if (recordMatchesKeywords(record, keywords)) {
        matches.add(dataFrame.getRowForColumns(row, tableColumns));
      }
    }
    LOGGER.fine(() -> "Search query '" + searchString + "' returned " + matches.size() + " rows.");
    return matches;
  }

  private static boolean recordMatchesKeywords(Map<String, String> record, List<String> keywords) {
    String searchable = String.join(
      " ",
      record.values().stream()
        .map(value -> value == null ? "" : value.toLowerCase(Locale.ROOT))
        .toList()
    );
    for (String keyword : keywords) {
      if (!searchable.contains(keyword)) {
        return false;
      }
    }
    return true;
  }

  private static Path resolveCsvPath() {
    String configuredPath = System.getProperty(AppConstants.Config.PATIENTS_CSV.key());
    if (configuredPath == null || configuredPath.isBlank()) {
      configuredPath = System.getenv(AppConstants.Config.PATIENTS_CSV.key());
    }

    if (configuredPath == null || configuredPath.isBlank()) {
      configuredPath = AppConstants.Config.PATIENTS_CSV.defaultValue();
    }

    return Paths.get(configuredPath.trim()).normalize();
  }
}
