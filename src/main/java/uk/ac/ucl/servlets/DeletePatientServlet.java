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
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(AppConstants.Routes.PATIENT_DELETE)
public class DeletePatientServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(DeletePatientServlet.class.getName());

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
      model.deletePatient(normalizedId);

      String message = AppConstants.Messages.PATIENT_DELETED_SUCCESS_PREFIX + normalizedId;
      String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
      response.sendRedirect(AppConstants.Routes.PATIENT_LIST + "?"
        + AppConstants.RequestParams.MESSAGE + "=" + encodedMessage);
    } catch (NoSuchElementException exception) {
      LOGGER.log(Level.WARNING, "Unknown patient id while deleting: '" + normalizedId + "'.", exception);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      request.setAttribute("errorMessage", AppConstants.Messages.PATIENT_NOT_FOUND_PREFIX + normalizedId);
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to delete patient '" + normalizedId + "'.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_SAVING_PATIENT_DATA_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while deleting patient '" + normalizedId + "'.", exception);
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

  private void forwardTo(String path, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    ServletContext context = getServletContext();
    RequestDispatcher dispatcher = context.getRequestDispatcher(path);
    dispatcher.forward(request, response);
  }
}
