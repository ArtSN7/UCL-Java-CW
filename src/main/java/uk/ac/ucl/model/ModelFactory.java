package uk.ac.ucl.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ModelFactory {
  private static final String DEFAULT_CSV_PATH = "data/patients100.csv";
  private static Model model;

  private ModelFactory() {
  }

  public static synchronized Model getModel() throws IOException {
    if (model == null) {
      model = new Model(resolveCsvPath());
    }

    return model;
  }

  private static Path resolveCsvPath() {
    String configuredPath = System.getProperty("PATIENTS_CSV");
    if (configuredPath == null || configuredPath.isBlank()) {
      configuredPath = System.getenv("PATIENTS_CSV");
    }

    if (configuredPath == null || configuredPath.isBlank()) {
      configuredPath = DEFAULT_CSV_PATH;
    }

    return Paths.get(configuredPath.trim()).normalize();
  }
}
