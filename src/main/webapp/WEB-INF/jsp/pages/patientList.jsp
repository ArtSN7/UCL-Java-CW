<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="uk.ac.ucl.config.AppConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!
  private static String escapeHtml(String value) {
    if (value == null) {
      return "";
    }
    StringBuilder escaped = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&' -> escaped.append("&amp;");
        case '<' -> escaped.append("&lt;");
        case '>' -> escaped.append("&gt;");
        case '"' -> escaped.append("&quot;");
        case '\'' -> escaped.append("&#39;");
        default -> escaped.append(c);
      }
    }
    return escaped.toString();
  }
%>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Patient Data App</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <div class="d-flex flex-wrap gap-3 align-items-start justify-content-between mb-3">
        <div>
          <h2 class="h3 mb-2">Patient Records</h2>
          <p class="text-secondary mb-0">Click a patient ID to view full details.</p>
        </div>
        <div class="d-flex flex-wrap gap-2">
          <a
            class="btn btn-primary rounded-pill px-4"
            href="/patient/add"
            target="_blank"
            rel="noopener"
          >Add Patient</a>
          <a
            class="btn btn-outline-secondary rounded-pill px-4"
            href="/patient/export/json"
          >Export to JSON</a>
        </div>
      </div>

      <%
        String message = (String) request.getAttribute("message");
        if (message != null && !message.isBlank()) {
      %>
      <div class="alert alert-success" role="alert"><%= escapeHtml(message) %></div>
      <%
        }

        String error = (String) request.getAttribute("error");
        if (error != null && !error.isBlank()) {
      %>
      <div class="alert alert-warning" role="alert"><%= escapeHtml(error) %></div>
      <%
        }

        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null && !errorMessage.isBlank()) {
      %>
      <div class="alert alert-danger alert-error" role="alert"><%= escapeHtml(errorMessage) %></div>
      <%
        }

        List<String> tableColumns = (List<String>) request.getAttribute("tableColumns");
        List<Map<String, String>> tableRows = (List<Map<String, String>>) request.getAttribute("tableRows");

        if (tableColumns != null && !tableColumns.isEmpty()) {
      %>
      <div class="table-responsive table-shell rounded-4">
        <table class="table table-striped table-hover align-middle mb-0">
          <thead class="table-light">
          <tr>
            <%
              for (String columnName : tableColumns) {
            %>
            <th><%= escapeHtml(columnName) %></th>
            <%
              }
            %>
          </tr>
          </thead>
          <tbody>
          <%
            if (tableRows != null && !tableRows.isEmpty()) {
              for (Map<String, String> rowData : tableRows) {
          %>
          <tr>
            <%
              for (String columnName : tableColumns) {
                String value = rowData.getOrDefault(columnName, "");
                if (AppConstants.CsvColumns.ID.equals(columnName) && !value.isBlank()) {
            %>
            <td><a class="id-link" href="/patient?id=<%= escapeHtml(value) %>"><%= escapeHtml(value) %></a></td>
            <%
                } else {
            %>
            <td><%= escapeHtml(value) %></td>
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
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
