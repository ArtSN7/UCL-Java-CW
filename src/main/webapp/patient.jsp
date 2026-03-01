<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/meta.jsp"/>
  <title>Patient Details</title>
</head>
<body>
<jsp:include page="/header.jsp"/>
<div class="main">
  <h2>Patient Details</h2>
  <%
    Map<String, String> patientDetails = (Map<String, String>) request.getAttribute("patientDetails");
    if (patientDetails != null && !patientDetails.isEmpty())
    {
  %>
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
  <%
    } else {
  %>
  <p>No patient details found.</p>
  <%
    }
  %>

  <p><a href="/patientList">Back to patient table</a></p>
</div>
<jsp:include page="/footer.jsp"/>
</body>
</html>
