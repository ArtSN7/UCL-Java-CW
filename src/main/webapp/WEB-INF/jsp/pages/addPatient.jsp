<%@ page import="java.util.List" %>
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

  private static String inputTypeFor(String columnName) {
    if (AppConstants.CsvColumns.BIRTHDATE.equals(columnName)
      || AppConstants.CsvColumns.DEATHDATE.equals(columnName)) {
      return "date";
    }
    return "text";
  }
%>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Add Patient</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <div class="d-flex flex-wrap gap-3 align-items-start justify-content-between mb-3">
        <div>
          <h2 class="h3 mb-2">Add New Patient</h2>
          <p class="text-secondary mb-0">Fill all fields. <strong>DEATHDATE</strong> can be blank for living patients.</p>
        </div>
        <a class="btn btn-outline-primary rounded-pill" href="/patientList">Back to Table</a>
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

        List<String> allColumns = (List<String>) request.getAttribute("allColumns");
        if (allColumns != null && !allColumns.isEmpty()) {
      %>
      <form action="/patient/add" method="post" class="row g-3 crud-form">
        <div class="col-12">
          <label class="form-label" for="generated-id">ID</label>
          <input id="generated-id" type="text" class="form-control" value="Auto-generated on submit" readonly/>
        </div>

        <%
          for (String columnName : allColumns) {
            if (AppConstants.CsvColumns.ID.equals(columnName)) {
              continue;
            }

            String inputType = inputTypeFor(columnName);
            String paramName = AppConstants.RequestParams.FIELD_PREFIX + columnName;
        %>
        <div class="col-md-6">
          <label class="form-label" for="<%= paramName %>"><%= escapeHtml(columnName) %></label>
          <input
            id="<%= paramName %>"
            name="<%= paramName %>"
            type="<%= inputType %>"
            class="form-control"
          />
        </div>
        <%
          }
        %>

        <div class="col-12 d-flex gap-2 pt-2">
          <button type="submit" class="btn btn-primary rounded-pill px-4">Add Patient</button>
        </div>
      </form>
      <%
        }
      %>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
