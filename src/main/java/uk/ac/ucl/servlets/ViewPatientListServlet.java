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


@WebServlet(AppConstants.Routes.PATIENT_LIST)
public class ViewPatientListServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ViewPatientListServlet.class.getName());

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            LOGGER.fine("Handling request for /patientList.");
            Model model = Model.getInstance();
            List<String> tableColumns = model.getTableColumns();
            List<Map<String, String>> tableRows = model.getPatientTableRows();
            request.setAttribute("tableColumns", tableColumns);
            request.setAttribute("tableRows", tableRows);
            LOGGER.fine(() -> "Prepared /patientList response with " + tableRows.size() + " rows.");
            ServletContext context = getServletContext();
            RequestDispatcher dispatch = context.getRequestDispatcher(AppConstants.Routes.PATIENT_LIST_JSP);
            dispatch.forward(request, response);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load patient data for /patientList.", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute(
                    "errorMessage",
                    AppConstants.Messages.ERROR_LOADING_PATIENT_DATA_PREFIX + e.getMessage()
            );
            ServletContext context = getServletContext();
            RequestDispatcher dispatch = context.getRequestDispatcher(AppConstants.Routes.ERROR_JSP);
            dispatch.forward(request, response);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while handling /patientList.", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorMessage", AppConstants.Messages.ERROR_LOADING_DATA_PREFIX + e.getMessage());
            ServletContext context = getServletContext();
            RequestDispatcher dispatch = context.getRequestDispatcher(AppConstants.Routes.ERROR_JSP);
            dispatch.forward(request, response);
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
