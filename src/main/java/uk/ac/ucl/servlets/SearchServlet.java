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

@WebServlet(AppConstants.Routes.RUN_SEARCH)
public class SearchServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String searchQuery = request.getParameter(AppConstants.RequestParams.SEARCH_STRING);
    String normalizedQuery = searchQuery == null ? "" : searchQuery.trim();

    try {
      Model model = Model.getInstance();
      List<String> tableColumns = model.getTableColumns();
      List<Map<String, String>> tableRows = normalizedQuery.isBlank()
        ? List.of()
        : model.searchPatients(normalizedQuery);

      request.setAttribute("searchString", normalizedQuery);
      request.setAttribute("tableColumns", tableColumns);
      request.setAttribute("tableRows", tableRows);
      request.setAttribute("resultCount", tableRows.size());

      if (normalizedQuery.isBlank()) {
        request.setAttribute("searchMessage", AppConstants.Messages.SEARCH_STRING_REQUIRED);
      }

      forwardTo(AppConstants.Routes.SEARCH_RESULTS_JSP, request, response);
    } catch (IllegalArgumentException exception) {
      LOGGER.log(Level.WARNING, "Search request validation failed.", exception);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute("searchString", normalizedQuery);
      request.setAttribute("tableColumns", List.of());
      request.setAttribute("tableRows", List.of());
      request.setAttribute("resultCount", 0);
      request.setAttribute("searchMessage", AppConstants.Messages.SEARCH_STRING_REQUIRED);
      forwardTo(AppConstants.Routes.SEARCH_RESULTS_JSP, request, response);
    } catch (IOException exception) {
      LOGGER.log(Level.SEVERE, "Failed to load model data for /runsearch.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute(
        "errorMessage",
        AppConstants.Messages.ERROR_RUNNING_SEARCH_PREFIX + exception.getMessage()
      );
      forwardTo(AppConstants.Routes.ERROR_JSP, request, response);
    } catch (RuntimeException exception) {
      LOGGER.log(Level.SEVERE, "Unexpected error while handling /runsearch.", exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", AppConstants.Messages.UNEXPECTED_ERROR_PREFIX + exception.getMessage());
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
    RequestDispatcher dispatcher = context.getRequestDispatcher(path);
    dispatcher.forward(request, response);
  }
}
