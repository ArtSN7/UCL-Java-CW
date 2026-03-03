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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(AppConstants.Routes.PATIENT_EXPORT_JSON)
public class ExportPatientsJsonServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(ExportPatientsJsonServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      Model model = Model.getInstance();
      Path temporaryJsonFile = Files.createTempFile("patients-export-", ".json");
      try {
        model.exportPatientsToJson(temporaryJsonFile);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=\"patients.json\"");
        response.setContentLengthLong(Files.size(temporaryJsonFile));
        Files.copy(temporaryJsonFile, response.getOutputStream());
        response.flushBuffer();
      } finally {
        Files.deleteIfExists(temporaryJsonFile);
      }
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to export patient data.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_EXPORTING_PATIENT_DATA_PREFIX + exception.getMessage()
      );
      forwardToError(request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while exporting patient data.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + exception.getMessage());
      forwardToError(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doGet(request, response);
  }

  private void forwardToError(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    if (response.isCommitted()) {
      return;
    }
    ServletContext context = getServletContext();
    RequestDispatcher dispatcher = context.getRequestDispatcher(AppConstants.Routes.ERROR_JSP);
    dispatcher.forward(request, response);
  }
}
