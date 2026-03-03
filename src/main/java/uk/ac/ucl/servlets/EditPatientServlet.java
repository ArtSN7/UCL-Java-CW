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
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(AppConstants.Routes.PATIENT_EDIT)
public class EditPatientServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(EditPatientServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String patientId = request.getParameter(AppConstants.RequestParams.PATIENT_ID);
    if (patientId == null || patientId.isBlank()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute("errorMessage", AppConstants.Messages.PATIENT_ID_REQUIRED);
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
      return;
    }

    String normalizedId = patientId.trim();

    try {
      Model model = Model.getInstance();
      Map<String, String> fields = readPatientFields(request, model.getAllColumns());
      model.updatePatient(normalizedId, fields);

      String message = AppConstants.Messages.PATIENT_UPDATED_SUCCESS_PREFIX + normalizedId;
      redirectToPatient(response, normalizedId, AppConstants.RequestParams.MESSAGE, message);
    } catch (IllegalArgumentException exception) {
      LOGGER.log(Level.WARNING, "Validation failed while updating patient '" + normalizedId + "'.", exception);
      redirectToPatient(response, normalizedId, AppConstants.RequestParams.ERROR, exception.getMessage());
    } catch (NoSuchElementException exception) {
      LOGGER.log(Level.WARNING, "Unknown patient id while updating: '" + normalizedId + "'.", exception);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      request.setAttribute("errorMessage", AppConstants.Messages.PATIENT_NOT_FOUND_PREFIX + normalizedId);
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to update patient '" + normalizedId + "'.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_SAVING_PATIENT_DATA_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while updating patient '" + normalizedId + "'.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + exception.getMessage());
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    String patientId = request.getParameter(AppConstants.RequestParams.PATIENT_ID);
    if (patientId == null || patientId.isBlank()) {
      response.sendRedirect(AppConstants.Routes.PATIENT_LIST);
      return;
    }
    response.sendRedirect(AppConstants.Routes.PATIENT + "?"
      + AppConstants.RequestParams.PATIENT_ID + "="
      + URLEncoder.encode(patientId.trim(), StandardCharsets.UTF_8));
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

  private static void redirectToPatient(
    HttpServletResponse response,
    String patientId,
    String queryKey,
    String queryValue
  ) throws IOException {
    String encodedId = URLEncoder.encode(patientId, StandardCharsets.UTF_8);
    String encodedValue = URLEncoder.encode(queryValue == null ? "" : queryValue, StandardCharsets.UTF_8);
    response.sendRedirect(AppConstants.Routes.PATIENT + "?"
      + AppConstants.RequestParams.PATIENT_ID + "=" + encodedId
      + "&" + queryKey + "=" + encodedValue);
  }

  private void forwardTo(String path, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    ServletContext context = getServletContext();
    RequestDispatcher dispatcher = context.getRequestDispatcher(path);
    dispatcher.forward(request, response);
  }
}
