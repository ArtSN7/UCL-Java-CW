package uk.ac.ucl.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.Model;
import uk.ac.ucl.model.ModelFactory;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

@WebServlet("/patient")
public class ViewPatientServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      String patientId = request.getParameter("id");
      Model model = ModelFactory.getModel();
      Map<String, String> patientDetails = model.getPatientDetails(patientId);

      request.setAttribute("patientDetails", patientDetails);
      forwardTo("/patient.jsp", request, response);
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute("errorMessage", "Patient id is required.");
      forwardTo("/error.jsp", request, response);
    } catch (NoSuchElementException e) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      request.setAttribute("errorMessage", "Patient not found: " + request.getParameter("id"));
      forwardTo("/error.jsp", request, response);
    } catch (IOException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", "Error loading patient data: " + e.getMessage());
      forwardTo("/error.jsp", request, response);
    } catch (RuntimeException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      request.setAttribute("errorMessage", "Unexpected error: " + e.getMessage());
      forwardTo("/error.jsp", request, response);
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
