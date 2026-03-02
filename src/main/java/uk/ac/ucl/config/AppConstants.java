package uk.ac.ucl.config;

import java.util.List;

public final class AppConstants {
  private AppConstants() {
  }

  public record StringConfig(String key, String defaultValue) {
  }

  public record IntConfig(String key, int defaultValue) {
  }

  public static final class Config {
    public static final IntConfig SERVER_PORT = new IntConfig("SERVER_PORT", 8080);
    public static final StringConfig WEBAPP_DIR = new StringConfig("WEBAPP_DIR", "src/main/webapp");
    public static final StringConfig CLASSES_DIR = new StringConfig("CLASSES_DIR", "target/classes");
    public static final StringConfig PATIENTS_CSV = new StringConfig("PATIENTS_CSV", "data/patients100.csv");

    private Config() {
    }
  }

  public static final class Routes {
    public static final String ROOT_CONTEXT = "";
    public static final String PATIENT_LIST = "/patientList";
    public static final String PATIENT_LIST_JSP = "/WEB-INF/jsp/pages/patientList.jsp";
    public static final String PATIENT = "/patient";
    public static final String PATIENT_JSP = "/WEB-INF/jsp/pages/patient.jsp";
    public static final String ERROR_JSP = "/WEB-INF/jsp/pages/error.jsp";

    private Routes() {
    }
  }

  public static final class Paths {
    public static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    private Paths() {
    }
  }

  public static final class CsvColumns {
    public static final String ID = "ID";
    public static final String FIRST = "FIRST";
    public static final String LAST = "LAST";
    public static final String CITY = "CITY";
    public static final String STATE = "STATE";
    public static final List<String> TABLE_COLUMNS = List.of(ID, FIRST, LAST, CITY, STATE);

    private CsvColumns() {
    }
  }

  public static final class RequestParams {
    public static final String PATIENT_ID = "id";

    private RequestParams() {
    }
  }

  public static final class Messages {
    public static final String PATIENT_ID_REQUIRED = "Patient ID is required.";
    public static final String PATIENT_NOT_FOUND_PREFIX = "Patient not found: ";
    public static final String ERROR_LOADING_PATIENT_DATA_PREFIX = "Error loading patient data: ";
    public static final String ERROR_LOADING_DATA_PREFIX = "Error loading data: ";
    public static final String UNEXPECTED_ERROR_PREFIX = "Unexpected error: ";

    private Messages() {
    }
  }
}
