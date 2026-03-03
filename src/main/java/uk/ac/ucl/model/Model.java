package uk.ac.ucl.model;

import uk.ac.ucl.config.AppConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

public class Model {
  private static final Logger LOGGER = Logger.getLogger(Model.class.getName());
  private static Model instance;

  private DataFrame dataFrame;
  private final List<String> tableColumns;
  private final Path csvPath;
  private final DataLoader dataLoader;

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

    this.dataLoader = new DataLoader();
    DataFrame loadedFrame;
    try {
      loadedFrame = dataLoader.loadCsv(normalizedPath);
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
    this.csvPath = normalizedPath;
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

  public List<String> getAllColumns() {
    return dataFrame.getColumnNames();
  }

  public List<Map<String, String>> getPatientTableRows() {
    return dataFrame.getRowsForColumns(tableColumns);
  }

  public List<Map<String, String>> getAllPatientRows() {
    return dataFrame.getRowsForColumns(getAllColumns());
  }

  public Map<String, String> getPatientDetails(String patientId) {
    String normalizedId = normalizePatientId(patientId);
    int row = dataFrame.findRowByValue(AppConstants.CsvColumns.ID, normalizedId);
    if (row < 0) {
      LOGGER.warning(() -> "Patient id not found: " + normalizedId + ".");
      throw new NoSuchElementException("Unknown patient id: " + normalizedId);
    }

    return dataFrame.getRecordDetails(row);
  }

  public synchronized String addPatient(Map<String, String> fields) throws IOException {
    DataFrame updatedDataFrame = dataFrame.deepCopy();
    List<String> columns = updatedDataFrame.getColumnNames();

    String patientId = generateUniquePatientId(updatedDataFrame);
    Map<String, String> newRow = buildValidatedRow(fields, columns, patientId);
    updatedDataFrame.appendRow(newRow);

    persistAndSwap(updatedDataFrame);
    LOGGER.info(() -> "Added patient " + patientId + ".");
    return patientId;
  }

  public synchronized void updatePatient(String patientId, Map<String, String> fields) throws IOException {
    String normalizedId = normalizePatientId(patientId);
    int rowIndex = dataFrame.findRowByValue(AppConstants.CsvColumns.ID, normalizedId);
    if (rowIndex < 0) {
      LOGGER.warning(() -> "Cannot update unknown patient id: " + normalizedId + ".");
      throw new NoSuchElementException("Unknown patient id: " + normalizedId);
    }

    DataFrame updatedDataFrame = dataFrame.deepCopy();
    List<String> columns = updatedDataFrame.getColumnNames();
    Map<String, String> updatedRow = buildValidatedRow(fields, columns, normalizedId);
    updatedDataFrame.updateRow(rowIndex, updatedRow);

    persistAndSwap(updatedDataFrame);
    LOGGER.info(() -> "Updated patient " + normalizedId + ".");
  }

  public synchronized void deletePatient(String patientId) throws IOException {
    String normalizedId = normalizePatientId(patientId);
    int rowIndex = dataFrame.findRowByValue(AppConstants.CsvColumns.ID, normalizedId);
    if (rowIndex < 0) {
      LOGGER.warning(() -> "Cannot delete unknown patient id: " + normalizedId + ".");
      throw new NoSuchElementException("Unknown patient id: " + normalizedId);
    }

    DataFrame updatedDataFrame = dataFrame.deepCopy();
    updatedDataFrame.removeRow(rowIndex);

    persistAndSwap(updatedDataFrame);
    LOGGER.info(() -> "Deleted patient " + normalizedId + ".");
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

  public StatisticsSummary getStatisticsSummary() {
    PersonMetric oldestLiving = null;
    PersonMetric youngestLiving = null;
    PersonMetric oldestOverall = null;
    PersonMetric youngestOverall = null;

    int aliveCount = 0;
    int deadCount = 0;
    int totalCount = dataFrame.getRowCount();

    for (int row = 0; row < totalCount; row++) {
      boolean alive = isAliveRow(row);
      if (alive) {
        aliveCount++;
      } else {
        deadCount++;
      }

      PersonMetric metric = buildPersonMetric(row);
      if (metric == null) {
        continue;
      }

      oldestOverall = pickOlder(oldestOverall, metric);
      youngestOverall = pickYounger(youngestOverall, metric);

      if (metric.alive()) {
        oldestLiving = pickOlder(oldestLiving, metric);
        youngestLiving = pickYounger(youngestLiving, metric);
      }
    }

    return new StatisticsSummary(
      oldestLiving,
      youngestLiving,
      oldestOverall,
      youngestOverall,
      aliveCount,
      deadCount,
      totalCount
    );
  }

  public List<String> getAvailableCities() {
    Set<String> cities = new TreeSet<>();
    for (int row = 0; row < dataFrame.getRowCount(); row++) {
      String city = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.CITY, row).trim();
      if (!city.isBlank()) {
        cities.add(city);
      }
    }
    return List.copyOf(cities);
  }

  public List<String> getPatientNamesByCity(String city, LifeStatusFilter filter) {
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException(AppConstants.Messages.CITY_REQUIRED);
    }

    LifeStatusFilter resolvedFilter = filter == null ? LifeStatusFilter.ALL : filter;
    String normalizedCity = city.trim();
    List<NameEntry> names = new ArrayList<>();

    for (int row = 0; row < dataFrame.getRowCount(); row++) {
      String rowCity = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.CITY, row).trim();
      if (!normalizedCity.equals(rowCity)) {
        continue;
      }

      boolean alive = isAliveRow(row);
      if (resolvedFilter == LifeStatusFilter.ALIVE && !alive) {
        continue;
      }
      if (resolvedFilter == LifeStatusFilter.DEAD && alive) {
        continue;
      }

      String firstName = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.FIRST, row).trim();
      String lastName = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.LAST, row).trim();
      String fullName = (firstName + " " + lastName).trim();
      if (fullName.isBlank()) {
        fullName = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.ID, row).trim();
      }

      names.add(new NameEntry(
        firstName.toLowerCase(Locale.ROOT),
        lastName.toLowerCase(Locale.ROOT),
        fullName
      ));
    }

    names.sort(Comparator
      .comparing(NameEntry::lastSort)
      .thenComparing(NameEntry::firstSort)
      .thenComparing(NameEntry::displayName));

    return names.stream()
      .map(NameEntry::displayName)
      .toList();
  }

  public Map<String, Integer> getAgeDistribution() {
    Map<Integer, Integer> ageCounts = new java.util.TreeMap<>();

    for (int row = 0; row < dataFrame.getRowCount(); row++) {
      PersonMetric metric = buildPersonMetric(row);
      if (metric == null) {
        continue;
      }
      ageCounts.merge(metric.ageYears(), 1, Integer::sum);
    }

    Map<String, Integer> distribution = new LinkedHashMap<>();
    for (Map.Entry<Integer, Integer> entry : ageCounts.entrySet()) {
      distribution.put(String.valueOf(entry.getKey()), entry.getValue());
    }
    return distribution;
  }

  public Map<String, Integer> getEthnicityDistribution() {
    return buildCategoryDistribution("ETHNICITY");
  }

  public Map<String, Integer> getRaceDistribution() {
    return buildCategoryDistribution("RACE");
  }

  private void persistAndSwap(DataFrame updatedDataFrame) throws IOException {
    dataLoader.saveCsv(csvPath, updatedDataFrame);
    dataFrame = updatedDataFrame;
  }

  private Map<String, Integer> buildCategoryDistribution(String columnName) {
    if (!dataFrame.hasColumn(columnName)) {
      return Map.of();
    }

    Map<String, Integer> countsByKey = new HashMap<>();
    Map<String, String> labelsByKey = new HashMap<>();

    for (int row = 0; row < dataFrame.getRowCount(); row++) {
      String rawValue = dataFrame.getValueOrEmpty(columnName, row).trim();
      String normalizedKey = rawValue.isBlank() ? "unknown" : rawValue.toLowerCase(Locale.ROOT);
      countsByKey.merge(normalizedKey, 1, Integer::sum);
      labelsByKey.putIfAbsent(normalizedKey, toDisplayCategory(rawValue));
    }

    return countsByKey.entrySet().stream()
      .sorted(Comparator
        .<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
        .reversed()
        .thenComparing(entry -> labelsByKey.get(entry.getKey())))
      .collect(
        LinkedHashMap::new,
        (distribution, entry) -> distribution.put(labelsByKey.get(entry.getKey()), entry.getValue()),
        LinkedHashMap::putAll
      );
  }

  private static String toDisplayCategory(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return "Unknown";
    }

    String[] words = rawValue.trim().toLowerCase(Locale.ROOT).split("\\s+");
    StringBuilder label = new StringBuilder();

    for (int index = 0; index < words.length; index++) {
      String word = words[index];
      if (word.isBlank()) {
        continue;
      }

      if (index > 0) {
        label.append(' ');
      }
      label.append(Character.toUpperCase(word.charAt(0)));
      if (word.length() > 1) {
        label.append(word.substring(1));
      }
    }

    return label.length() == 0 ? "Unknown" : label.toString();
  }

  private static String generateUniquePatientId(DataFrame dataFrame) {
    String patientId = UUID.randomUUID().toString();
    while (dataFrame.findRowByValue(AppConstants.CsvColumns.ID, patientId) >= 0) {
      patientId = UUID.randomUUID().toString();
    }
    return patientId;
  }

  private static Map<String, String> buildValidatedRow(
    Map<String, String> fields,
    List<String> columns,
    String patientId
  ) {
    if (fields == null) {
      throw new IllegalArgumentException("Patient fields cannot be null.");
    }

    Map<String, String> row = new LinkedHashMap<>();
    for (String columnName : columns) {
      if (AppConstants.CsvColumns.ID.equals(columnName)) {
        row.put(columnName, patientId);
        continue;
      }

      if (!fields.containsKey(columnName)) {
        throw new IllegalArgumentException(AppConstants.Messages.FIELD_REQUIRED_PREFIX + columnName);
      }

      String value = normalizeFieldValue(fields.get(columnName));
      if (value.isBlank() && !AppConstants.CsvColumns.DEATHDATE.equals(columnName)) {
        throw new IllegalArgumentException(AppConstants.Messages.FIELD_REQUIRED_PREFIX + columnName);
      }

      row.put(columnName, value);
    }

    return row;
  }

  private static String normalizePatientId(String patientId) {
    if (patientId == null || patientId.isBlank()) {
      LOGGER.warning("Patient request had a blank id.");
      throw new IllegalArgumentException("Patient id cannot be blank.");
    }
    return patientId.trim();
  }

  private static String normalizeFieldValue(String value) {
    return value == null ? "" : value.trim();
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

  private boolean isAliveRow(int row) {
    return dataFrame.getValueOrEmpty(AppConstants.CsvColumns.DEATHDATE, row).isBlank();
  }

  private PersonMetric buildPersonMetric(int row) {
    String id = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.ID, row).trim();
    String firstName = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.FIRST, row).trim();
    String lastName = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.LAST, row).trim();
    String birthDateText = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.BIRTHDATE, row).trim();
    String deathDateText = dataFrame.getValueOrEmpty(AppConstants.CsvColumns.DEATHDATE, row).trim();

    if (birthDateText.isBlank()) {
      LOGGER.warning(() -> "Skipping patient '" + id + "' due to missing BIRTHDATE.");
      return null;
    }

    LocalDate birthDate = parseDate(AppConstants.CsvColumns.BIRTHDATE, id, birthDateText);
    if (birthDate == null) {
      return null;
    }

    boolean alive = deathDateText.isBlank();
    LocalDate endDate = LocalDate.now();
    if (!alive) {
      LocalDate deathDate = parseDate(AppConstants.CsvColumns.DEATHDATE, id, deathDateText);
      if (deathDate == null) {
        return null;
      }
      if (deathDate.isBefore(birthDate)) {
        LOGGER.warning(() -> "Skipping patient '" + id + "' due to DEATHDATE before BIRTHDATE.");
        return null;
      }
      endDate = deathDate;
    }

    int ageYears = Period.between(birthDate, endDate).getYears();
    if (ageYears < 0) {
      LOGGER.warning(() -> "Skipping patient '" + id + "' due to negative computed age.");
      return null;
    }

    return new PersonMetric(
      id,
      firstName,
      lastName,
      birthDateText,
      deathDateText,
      ageYears,
      alive
    );
  }

  private LocalDate parseDate(String columnName, String patientId, String value) {
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException exception) {
      LOGGER.warning(() -> "Skipping patient '" + patientId + "' due to invalid "
        + columnName + ": '" + value + "'.");
      return null;
    }
  }

  private static PersonMetric pickOlder(PersonMetric current, PersonMetric candidate) {
    if (current == null) {
      return candidate;
    }

    int dateComparison = candidate.birthDate().compareTo(current.birthDate());
    if (dateComparison < 0) {
      return candidate;
    }
    if (dateComparison > 0) {
      return current;
    }

    return candidate.id().compareTo(current.id()) < 0 ? candidate : current;
  }

  private static PersonMetric pickYounger(PersonMetric current, PersonMetric candidate) {
    if (current == null) {
      return candidate;
    }

    int dateComparison = candidate.birthDate().compareTo(current.birthDate());
    if (dateComparison > 0) {
      return candidate;
    }
    if (dateComparison < 0) {
      return current;
    }

    return candidate.id().compareTo(current.id()) < 0 ? candidate : current;
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

  private record NameEntry(String firstSort, String lastSort, String displayName) {
  }
}
