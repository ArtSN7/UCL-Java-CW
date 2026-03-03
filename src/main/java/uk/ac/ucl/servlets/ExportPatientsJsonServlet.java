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
import java.util.List;
import java.util.Map;
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
      List<Map<String, String>> allRows = model.getAllPatientRows();

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      response.setHeader("Content-Disposition", "attachment; filename=\"patients.json\"");
      response.getWriter().write(toJson(allRows));
      response.getWriter().flush();
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
    ServletContext context = getServletContext();
    RequestDispatcher dispatcher = context.getRequestDispatcher(AppConstants.Routes.ERROR_JSP);
    dispatcher.forward(request, response);
  }

  private static String toJson(List<Map<String, String>> rows) {
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");

    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      Map<String, String> row = rows.get(rowIndex);
      builder.append("  {");

      int fieldIndex = 0;
      for (Map.Entry<String, String> entry : row.entrySet()) {
        builder.append("\n    \"")
          .append(escapeJson(entry.getKey()))
          .append("\": \"")
          .append(escapeJson(entry.getValue()))
          .append("\"");

        if (fieldIndex < row.size() - 1) {
          builder.append(',');
        }
        fieldIndex++;
      }

      builder.append("\n  }");
      if (rowIndex < rows.size() - 1) {
        builder.append(',');
      }
      builder.append('\n');
    }

    builder.append(']');
    return builder.toString();
  }

  private static String escapeJson(String value) {
    if (value == null) {
      return "";
    }

    StringBuilder escaped = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      switch (character) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (character < 0x20) {
            escaped.append(String.format("\\u%04x", (int) character));
          } else {
            escaped.append(character);
          }
        }
      }
    }
    return escaped.toString();
  }
}
