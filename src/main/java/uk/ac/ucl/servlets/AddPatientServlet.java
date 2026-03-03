package uk.ac.ucl.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.config.AppConstants;
import uk.ac.ucl.model.Model;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(AppConstants.Routes.PATIENT_ADD)
public class AddPatientServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(AddPatientServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      Model model = Model.getInstance();
      Map<String, String> fields = readPatientFields(request, model.getAllColumns());
      String patientId = model.addPatient(fields);
      String message = AppConstants.Messages.PATIENT_CREATED_SUCCESS_PREFIX + patientId;
      redirectWithQuery(response, AppConstants.Routes.PATIENT_LIST, AppConstants.RequestParams.MESSAGE, message);
    } catch (IllegalArgumentException exception) {
      LOGGER.log(Level.WARNING, "Validation failed while adding patient.", exception);
      redirectWithQuery(
        response,
        AppConstants.Routes.PATIENT_ADD,
        AppConstants.RequestParams.ERROR,
        exception.getMessage()
      );
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to add patient.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_SAVING_PATIENT_DATA_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while adding patient.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + exception.getMessage());
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      Model model = Model.getInstance();
      request.setAttribute("allColumns", model.getAllColumns());
      attachFlashMessage(request);
      forwardTo(AppConstants.Routes.PATIENT_ADD_JSP, request, response);
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to load patient data for add page.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_LOADING_PATIENT_DATA_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while loading add page.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + exception.getMessage());
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    }
  }

  private static Map<String, String> readPatientFields(HttpServletRequest request, List<String> allColumns) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (String columnName : allColumns) {
      if (AppConstants.CsvColumns.ID.equals(columnName)) {
        continue;
      }

      String paramName = AppConstants.RequestParams.FIELD_PREFIX + columnName;
      fields.put(columnName, request.getParameter(paramName));
    }
    return fields;
  }

  private static void redirectWithQuery(
    HttpServletResponse response,
    String path,
    String queryKey,
    String queryValue
  ) throws IOException {
    String encodedValue = URLEncoder.encode(queryValue == null ? "" : queryValue, StandardCharsets.UTF_8);
    response.sendRedirect(path + "?" + queryKey + "=" + encodedValue);
  }

  private void forwardTo(String path, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    ServletContext context = getServletContext();
    RequestDispatcher dispatcher = context.getRequestDispatcher(path);
    dispatcher.forward(request, response);
  }

  private static void attachFlashMessage(HttpServletRequest request) {
    String message = request.getParameter(AppConstants.RequestParams.MESSAGE);
    if (message != null && !message.isBlank()) {
      request.setAttribute("message", message.trim());
    }

    String error = request.getParameter(AppConstants.RequestParams.ERROR);
    if (error != null && !error.isBlank()) {
      request.setAttribute("error", error.trim());
    }
  }
}
