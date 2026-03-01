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
<div class="main">
  <h2>Patient Records</h2>
  <%
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage != null)
    {
  %>
      <p style="color: red;"><%= errorMessage %></p>
  <%
    }
    List<String> tableColumns = (List<String>) request.getAttribute("tableColumns");
    List<Map<String, String>> tableRows = (List<Map<String, String>>) request.getAttribute("tableRows");

    if (tableColumns != null && !tableColumns.isEmpty())
    {
  %>
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
      <td><a href="/patient?id=<%= value %>"><%= value %></a></td>
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
      <td colspan="<%= tableColumns.size() %>">No patient records found.</td>
    </tr>
    <%
      }
    %>
    </tbody>
  </table>
  <%
    } else {
  %>
  <p>No patient records found.</p>
  <%
    }
  %>
</div>
<jsp:include page="/footer.jsp"/>
</body>
</html>
