<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Patient Details</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <h2 class="h3 mb-2">Patient Details</h2>
      <p class="text-secondary mb-4">Complete record view for the selected patient.</p>
      <%
        Map<String, String> patientDetails = (Map<String, String>) request.getAttribute("patientDetails");
        if (patientDetails != null && !patientDetails.isEmpty())
        {
      %>
      <div class="table-responsive table-shell rounded-4">
        <table class="table table-striped table-hover align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th>Field</th>
            <th>Value</th>
          </tr>
          </thead>
          <tbody>
          <%
            for (Map.Entry<String, String> entry : patientDetails.entrySet())
            {
          %>
          <tr>
            <td><%= entry.getKey() %></td>
            <td><%= entry.getValue() %></td>
          </tr>
          <%
            }
          %>
          </tbody>
        </table>
      </div>
      <%
        } else {
      %>
      <p class="text-secondary mb-0">No patient details found.</p>
      <%
        }
      %>

      <p class="mt-4 mb-0"><a class="btn btn-outline-primary rounded-pill" href="/patientList">Back to patient table</a></p>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
