package uk.ac.ucl.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JSONWriter {
  private final ObjectMapper objectMapper;

  public JSONWriter() {
    this.objectMapper = new ObjectMapper();
  }

  public void writeJson(Path outputPath, DataFrame dataFrame) throws IOException {
    Path targetPath = Objects.requireNonNull(outputPath, "Output path cannot be null.").toAbsolutePath().normalize();
    DataFrame sourceFrame = Objects.requireNonNull(dataFrame, "DataFrame cannot be null.");

    Path parentDirectory = targetPath.getParent();
    if (parentDirectory == null) {
      parentDirectory = Path.of(".").toAbsolutePath().normalize();
    }
    Files.createDirectories(parentDirectory);

    Path temporaryFile = Files.createTempFile(parentDirectory, "patients-", ".json.tmp");
    try {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporaryFile.toFile(), buildRows(sourceFrame));
      moveAtomically(temporaryFile, targetPath);
    } finally {
      Files.deleteIfExists(temporaryFile);
    }
  }

  private static List<Map<String, String>> buildRows(DataFrame dataFrame) {
    List<String> columns = dataFrame.getColumnNames();
    int rowCount = dataFrame.getRowCount();
    List<Map<String, String>> rows = new ArrayList<>(rowCount);

    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      Map<String, String> row = new LinkedHashMap<>();
      for (String columnName : columns) {
        row.put(columnName, dataFrame.getValueOrEmpty(columnName, rowIndex));
      }
      rows.add(row);
    }
    return rows;
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
}
