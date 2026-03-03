package uk.ac.ucl.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.config.AppConstants;
import uk.ac.ucl.model.LifeStatusFilter;
import uk.ac.ucl.model.Model;
import uk.ac.ucl.model.StatisticsSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(AppConstants.Routes.STATISTICS)
public class ViewStatisticsServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(ViewStatisticsServlet.class.getName());
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String selectedCity = readTrimmedParam(request, AppConstants.RequestParams.CITY);
    LifeStatusFilter statusFilter = LifeStatusFilter.from(
      request.getParameter(AppConstants.RequestParams.STATUS)
    );

    try {
      Model model = Model.getInstance();
      StatisticsSummary statisticsSummary = model.getStatisticsSummary();
      List<String> availableCities = model.getAvailableCities();
      Map<String, Integer> ageDistribution = model.getAgeDistribution();
      Map<String, Integer> ethnicityDistribution = model.getEthnicityDistribution();
      Map<String, Integer> raceDistribution = model.getRaceDistribution();

      String cityMessage = null;
      List<String> patientNames = List.of();

      if (!selectedCity.isBlank()) {
        if (!availableCities.contains(selectedCity)) {
          cityMessage = AppConstants.Messages.CITY_NOT_FOUND_PREFIX + selectedCity;
          LOGGER.warning(() -> "Statistics request used unknown city '" + selectedCity + "'.");
        } else {
          patientNames = model.getPatientNamesByCity(selectedCity, statusFilter);
        }
      }

      request.setAttribute("statisticsSummary", statisticsSummary);
      request.setAttribute("availableCities", availableCities);
      request.setAttribute("selectedCity", selectedCity);
      request.setAttribute("selectedStatus", statusFilter.toRequestValue());
      request.setAttribute("selectedStatusLabel", toStatusLabel(statusFilter));
      request.setAttribute("patientNames", patientNames);
      request.setAttribute("resultCount", patientNames.size());
      request.setAttribute("cityMessage", cityMessage);
      attachChartAttributes(request, "age", ageDistribution);
      attachChartAttributes(request, "ethnicity", ethnicityDistribution);
      attachChartAttributes(request, "race", raceDistribution);

      forwardTo(AppConstants.Routes.STATISTICS_JSP, request, response);
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to load statistics data.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_LOADING_STATISTICS_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while handling /statistics.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doGet(request, response);
  }

  private static String readTrimmedParam(HttpServletRequest request, String paramName) {
    String value = request.getParameter(paramName);
    return value == null ? "" : value.trim();
  }

  private static String toStatusLabel(LifeStatusFilter filter) {
    return switch (filter) {
      case ALIVE -> "Alive";
      case DEAD -> "Dead";
      default -> "All";
    };
  }

  private static void attachChartAttributes(
    HttpServletRequest request,
    String prefix,
    Map<String, Integer> distribution
  ) {
    List<String> labels = new ArrayList<>(distribution.keySet());
    List<Integer> counts = new ArrayList<>(distribution.values());
    request.setAttribute(prefix + "Labels", labels);
    request.setAttribute(prefix + "Counts", counts);
    request.setAttribute(prefix + "LabelsJson", toSafeJson(labels));
    request.setAttribute(prefix + "CountsJson", toSafeJson(counts));
  }

  private static String toSafeJson(Object value) {
    try {
      String json = OBJECT_MAPPER.writeValueAsString(value);
      // Protect inline <script> contexts from accidental closing tags.
      return json
        .replace("<", "\\u003c")
        .replace(">", "\\u003e")
        .replace("&", "\\u0026");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to serialize chart data as JSON.", exception);
    }
  }

  private void forwardTo(String path, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    ServletContext context = getServletContext();
    RequestDispatcher dispatcher = context.getRequestDispatcher(path);
    dispatcher.forward(request, response);
  }
}
