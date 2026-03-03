<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
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

  private static String encodeUrlParam(String value) {
    if (value == null) {
      return "";
    }
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
%>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Search Results</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <h2 class="h3 mb-2">Search Results</h2>
      <%
        String searchString = (String) request.getAttribute("searchString");
        if (searchString == null) {
          searchString = "";
        }
      %>
      <p class="text-secondary mb-4">Query: <strong><%= escapeHtml(searchString) %></strong></p>
      <%
        String searchMessage = (String) request.getAttribute("searchMessage");
        if (searchMessage != null && !searchMessage.isBlank()) {
      %>
      <div class="alert alert-warning" role="alert"><%= escapeHtml(searchMessage) %></div>
      <%
        }

        List<String> tableColumns = (List<String>) request.getAttribute("tableColumns");
        List<Map<String, String>> tableRows = (List<Map<String, String>>) request.getAttribute("tableRows");
        Integer resultCount = (Integer) request.getAttribute("resultCount");
        int count = resultCount == null ? 0 : resultCount;
      %>
      <p class="mb-3"><strong><%= count %></strong> matching record(s).</p>
      <%
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
                if ("ID".equals(columnName) && !value.isBlank()) {
            %>
            <td>
              <a class="id-link" href="/patient?id=<%= encodeUrlParam(value) %>"><%= escapeHtml(value) %></a>
            </td>
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
            <td colspan="<%= tableColumns.size() %>" class="text-secondary">No matching records found.</td>
          </tr>
          <%
            }
          %>
          </tbody>
        </table>
      </div>
      <%
        }
      %>
      <p class="mt-4 mb-0">
        <a class="btn btn-outline-primary rounded-pill" href="/search">New Search</a>
      </p>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
