package uk.ac.ucl.model;

import uk.ac.ucl.config.AppConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class ModelFactory {
  private static final Logger LOGGER = Logger.getLogger(ModelFactory.class.getName());
  private static Model model;

  private ModelFactory() {
  }

  public static synchronized Model getModel() throws IOException {
    if (model == null) {
      Path csvPath = resolveCsvPath();
      LOGGER.info(() -> "Creating model using CSV file: " + csvPath + ".");
      model = new Model(csvPath);
    } else {
      LOGGER.fine("Reusing existing Model singleton.");
    }

    return model;
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
