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
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <h2 class="h3 mb-2">Patient Records</h2>
      <p class="text-secondary mb-4">Selected fields are shown in table form. Click a patient ID to view full details.</p>
      <%
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null)
        {
      %>
      <div class="alert alert-danger alert-error" role="alert"><%= errorMessage %></div>
      <%
        }
        List<String> tableColumns = (List<String>) request.getAttribute("tableColumns");
        List<Map<String, String>> tableRows = (List<Map<String, String>>) request.getAttribute("tableRows");

        if (tableColumns != null && !tableColumns.isEmpty())
        {
      %>
      <div class="table-responsive table-shell rounded-4">
        <table class="table table-striped table-hover align-middle mb-0">
          <thead class="table-light">
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
            <td><a class="id-link" href="patient?id=<%= value %>"><%= value %></a></td>
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
            <td colspan="<%= tableColumns.size() %>" class="text-secondary">No patient records found.</td>
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
      <p class="text-secondary mb-0">No patient records found.</p>
      <%
        }
      %>
    </div>
  </section>
</main>
<jsp:include page="/footer.jsp"/>
</body>
</html>
