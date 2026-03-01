<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/meta.jsp"/>
  <title>Patient Data App - Error</title>
</head>
<body>
<jsp:include page="/header.jsp"/>
<div class="main">
  <h1>Something went wrong</h1>
  <%
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage == null || errorMessage.trim().isEmpty())
    {
      errorMessage = "An unexpected error occurred.";
    }
  %>
  <p style="color: red;"><%= errorMessage %></p>
</div>
<jsp:include page="/footer.jsp"/>
</body>
</html>
