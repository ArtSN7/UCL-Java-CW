package uk.ac.ucl.main;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import uk.ac.ucl.config.AppConstants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  private Main() {
  }

  public static void main(String[] args) {
    Tomcat tomcat = new Tomcat();

    try {
      int port = readPort();
      Path webappDirectory = readPathSetting(AppConstants.Config.WEBAPP_DIR);
      Path classesDirectory = readPathSetting(AppConstants.Config.CLASSES_DIR);
      LOGGER.info(() -> "Starting server with port=" + port
        + ", webappDir=" + webappDirectory.toAbsolutePath()
        + ", classesDir=" + classesDirectory.toAbsolutePath() + ".");

      requireDirectory(webappDirectory, "Webapp directory");
      requireDirectory(classesDirectory, "Classes directory");

      tomcat.setPort(port);
      tomcat.getConnector();

      Context context = tomcat.addWebapp(
        AppConstants.Routes.ROOT_CONTEXT,
        webappDirectory.toAbsolutePath().toString()
      );
      configureClassResources(context, classesDirectory);
      addShutdownHook(tomcat);

      tomcat.start();
      LOGGER.info("Server started on http://localhost:" + port);
      tomcat.getServer().await();
    } catch (Exception exception) {
      LOGGER.log(Level.SEVERE, "Failed to start embedded Tomcat.", exception);
    }
  }

  private static int readPort() {
    String configuredPort = readSetting(AppConstants.Config.SERVER_PORT.key());
    if (configuredPort == null) {
      return AppConstants.Config.SERVER_PORT.defaultValue();
    }

    try {
      return Integer.parseInt(configuredPort);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(
        "Invalid " + AppConstants.Config.SERVER_PORT.key() + ": " + configuredPort,
        exception
      );
    }
  }

  private static Path readPathSetting(AppConstants.StringConfig setting) {
    String configured = readSetting(setting.key());
    String pathValue = configured == null ? setting.defaultValue() : configured;
    return Paths.get(pathValue).normalize();
  }

  private static String readSetting(String settingName) {
    String value = System.getProperty(settingName);
    if (value == null || value.isBlank()) {
      value = System.getenv(settingName);
    }

    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private static void requireDirectory(Path directory, String label) {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      throw new IllegalArgumentException(label + " does not exist: " + directory.toAbsolutePath());
    }
  }

  private static void configureClassResources(Context context, Path classesDirectory) {
    WebResourceRoot resources = new StandardRoot(context);
    resources.addPreResources(new DirResourceSet(
      resources,
      AppConstants.Paths.WEB_INF_CLASSES,
      classesDirectory.toAbsolutePath().toString(),
      "/"
    ));
    context.setResources(resources);
  }

  private static void addShutdownHook(Tomcat tomcat) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        tomcat.stop();
        tomcat.destroy();
      } catch (Exception exception) {
        LOGGER.log(Level.WARNING, "Failed while shutting down Tomcat.", exception);
      }
    }));
  }
}
