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
    public static final String PATIENT_ADD = "/patient/add";
    public static final String PATIENT_ADD_JSP = "/WEB-INF/jsp/pages/addPatient.jsp";
    public static final String PATIENT_EDIT = "/patient/edit";
    public static final String PATIENT_DELETE = "/patient/delete";
    public static final String PATIENT_EXPORT_JSON = "/patient/export/json";
    public static final String SEARCH = "/search";
    public static final String SEARCH_JSP = "/WEB-INF/jsp/pages/search.jsp";
    public static final String RUN_SEARCH = "/runsearch";
    public static final String SEARCH_RESULTS_JSP = "/WEB-INF/jsp/pages/searchResult.jsp";
    public static final String STATISTICS = "/statistics";
    public static final String STATISTICS_JSP = "/WEB-INF/jsp/pages/statistics.jsp";
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
    public static final String BIRTHDATE = "BIRTHDATE";
    public static final String DEATHDATE = "DEATHDATE";
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
    public static final String SEARCH_STRING = "searchstring";
    public static final String CITY = "city";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String ERROR = "error";
    public static final String FIELD_PREFIX = "field_";

    private RequestParams() {
    }
  }

  public static final class Messages {
    public static final String PATIENT_ID_REQUIRED = "Patient ID is required.";
    public static final String FIELD_REQUIRED_PREFIX = "Field is required: ";
    public static final String SEARCH_STRING_REQUIRED = "Please enter one or more search words.";
    public static final String CITY_REQUIRED = "Select a city to view people.";
    public static final String CITY_NOT_FOUND_PREFIX = "City not found: ";
    public static final String PATIENT_NOT_FOUND_PREFIX = "Patient not found: ";
    public static final String PATIENT_CREATED_SUCCESS_PREFIX = "Patient created with ID: ";
    public static final String PATIENT_UPDATED_SUCCESS_PREFIX = "Patient updated: ";
    public static final String PATIENT_DELETED_SUCCESS_PREFIX = "Patient deleted: ";
    public static final String ERROR_LOADING_PATIENT_DATA_PREFIX = "Error loading patient data: ";
    public static final String ERROR_SAVING_PATIENT_DATA_PREFIX = "Error saving patient data: ";
    public static final String ERROR_EXPORTING_PATIENT_DATA_PREFIX = "Error exporting patient data: ";
    public static final String ERROR_RUNNING_SEARCH_PREFIX = "Error running search: ";
    public static final String ERROR_LOADING_STATISTICS_PREFIX = "Error loading statistics: ";
    public static final String ERROR_LOADING_DATA_PREFIX = "Error loading data: ";
    public static final String UNEXPECTED_ERROR_PREFIX = "Unexpected error: ";

    private Messages() {
    }
  }
}
