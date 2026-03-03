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
  <title>Patient Details</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card mb-4">
    <div class="card-body p-4">
      <h2 class="h3 mb-2">Patient Details</h2>
      <p class="text-secondary mb-4">Complete record view for the selected patient.</p>

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

        Map<String, String> patientDetails = (Map<String, String>) request.getAttribute("patientDetails");
      %>

      <%
        if (patientDetails != null && !patientDetails.isEmpty()) {
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
            for (Map.Entry<String, String> entry : patientDetails.entrySet()) {
          %>
          <tr>
            <td><%= escapeHtml(entry.getKey()) %></td>
            <td><%= escapeHtml(entry.getValue()) %></td>
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
    </div>
  </section>

  <%
    String patientId = patientDetails == null ? "" : patientDetails.getOrDefault(AppConstants.CsvColumns.ID, "");
    List<String> allColumns = (List<String>) request.getAttribute("allColumns");
    if (patientDetails != null && !patientDetails.isEmpty() && allColumns != null && !allColumns.isEmpty()) {
  %>
  <section class="card border-0 shadow-sm data-card mb-4">
    <div class="card-body p-4">
      <h3 class="h4 mb-3">Edit Patient</h3>
      <p class="text-secondary mb-4">Update any field and save changes. <strong>ID</strong> is read-only.</p>

      <form action="/patient/edit" method="post" class="row g-3 crud-form">
        <input type="hidden" name="id" value="<%= escapeHtml(patientId) %>"/>

        <div class="col-12">
          <label class="form-label" for="patient-id">ID</label>
          <input id="patient-id" type="text" class="form-control" value="<%= escapeHtml(patientId) %>" readonly/>
        </div>

        <%
          for (String columnName : allColumns) {
            if (AppConstants.CsvColumns.ID.equals(columnName)) {
              continue;
            }

            String fieldName = AppConstants.RequestParams.FIELD_PREFIX + columnName;
            String value = patientDetails.getOrDefault(columnName, "");
            String inputType = inputTypeFor(columnName);
        %>
        <div class="col-md-6">
          <label class="form-label" for="<%= fieldName %>"><%= escapeHtml(columnName) %></label>
          <input
            id="<%= fieldName %>"
            name="<%= fieldName %>"
            type="<%= inputType %>"
            class="form-control"
            value="<%= escapeHtml(value) %>"
          />
        </div>
        <%
          }
        %>

        <div class="col-12 d-flex gap-2 pt-2">
          <button type="submit" class="btn btn-primary rounded-pill px-4">Save Changes</button>
        </div>
      </form>
    </div>
  </section>

  <section class="card border-0 shadow-sm data-card mb-4">
    <div class="card-body p-4">
      <h3 class="h4 mb-3 text-danger">Delete Patient</h3>
      <p class="text-secondary mb-3">This action permanently removes the patient record from the CSV dataset.</p>
      <form
        action="/patient/delete"
        method="post"
        onsubmit="return confirm('Delete this patient permanently?');"
      >
        <input type="hidden" name="id" value="<%= escapeHtml(patientId) %>"/>
        <button type="submit" class="btn btn-danger rounded-pill px-4">Delete Patient</button>
      </form>
    </div>
  </section>
  <%
    }
  %>

  <p class="mt-4 mb-0"><a class="btn btn-outline-primary rounded-pill" href="/patientList">Back to patient table</a></p>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
