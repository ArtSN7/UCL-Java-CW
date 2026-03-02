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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(AppConstants.Routes.PATIENT)
public class ViewPatientServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(ViewPatientServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      String patientId = request.getParameter(AppConstants.RequestParams.PATIENT_ID);
      LOGGER.fine(() -> "Handling request for /patient with id='" + patientId + "'.");
      Model model = Model.getInstance();
      Map<String, String> patientDetails = model.getPatientDetails(patientId);

      request.setAttribute("patientDetails", patientDetails);
      forwardTo(AppConstants.Routes.PATIENT_JSP, request, response);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Invalid patient id supplied for /patient.", e);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute("errorMessage", AppConstants.Messages.PATIENT_ID_REQUIRED);
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (NoSuchElementException e) {
      LOGGER.log(Level.WARNING, "Patient id not found for /patient.", e);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.PATIENT_NOT_FOUND_PREFIX + request.getParameter(AppConstants.RequestParams.PATIENT_ID)
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to load patient data for /patient.", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_LOADING_PATIENT_DATA_PREFIX + e.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Unexpected error while handling /patient.", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + e.getMessage());
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doGet(request, response);
  }

  private void forwardTo(String path, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    ServletContext context = getServletContext();
    RequestDispatcher dispatch = context.getRequestDispatcher(path);
    dispatch.forward(request, response);
  }
}
