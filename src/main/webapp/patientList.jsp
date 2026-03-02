<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/meta.jsp"/>
  <title>Patient Data App</title>
</head>
<body>
<jsp:include page="/header.jsp"/>
<main class="app-shell main-content page-enter">
  <section class="surface-card">
    <h2 class="page-title">Patient Records</h2>
    <p class="muted">Selected fields are shown in table form. Click a patient ID to view full details.</p>
    <%
      String errorMessage = (String) request.getAttribute("errorMessage");
      if (errorMessage != null)
      {
    %>
    <div class="alert-error"><%= errorMessage %></div>
    <%
      }
      List<String> tableColumns = (List<String>) request.getAttribute("tableColumns");
      List<Map<String, String>> tableRows = (List<Map<String, String>>) request.getAttribute("tableRows");

      if (tableColumns != null && !tableColumns.isEmpty())
      {
    %>
    <div class="table-wrap">
      <table>
        <thead>
        <tr>
          <%
            for (String columnName : tableColumns)
            {
          %>
          <th><%= columnName %></th>
          <%
            }
          %>
        </tr>
        </thead>
        <tbody>
        <%
          if (tableRows != null && !tableRows.isEmpty())
          {
            for (Map<String, String> rowData : tableRows)
            {
        %>
        <tr>
          <%
            for (String columnName : tableColumns)
            {
              String value = rowData.getOrDefault(columnName, "");
              if ("ID".equals(columnName) && !value.isBlank())
              {
          %>
          <td><a class="id-link" href="/patient?id=<%= value %>"><%= value %></a></td>
          <%
              } else {
          %>
          <td><%= value %></td>
          <%
              }
            }
          %>
        </tr>
        <%
            }
          } else {
        %>
        <tr>
          <td colspan="<%= tableColumns.size() %>" class="empty-state">No patient records found.</td>
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
    <p class="empty-state">No patient records found.</p>
    <%
      }
    %>
  </section>
</main>
<jsp:include page="/footer.jsp"/>
</body>
</html>
