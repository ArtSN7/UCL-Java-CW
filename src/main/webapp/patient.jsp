<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/meta.jsp"/>
  <title>Patient Details</title>
</head>
<body>
<jsp:include page="/header.jsp"/>
<main class="app-shell main-content page-enter">
  <section class="surface-card">
    <h2 class="page-title">Patient Details</h2>
    <p class="muted">Complete record view for the selected patient.</p>
    <%
      Map<String, String> patientDetails = (Map<String, String>) request.getAttribute("patientDetails");
      if (patientDetails != null && !patientDetails.isEmpty())
      {
    %>
    <div class="table-wrap">
      <table>
        <thead>
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
    <p class="empty-state">No patient details found.</p>
    <%
      }
    %>

    <p><a class="btn btn-secondary" href="/patientList">Back to patient table</a></p>
  </section>
</main>
<jsp:include page="/footer.jsp"/>
</body>
</html>
