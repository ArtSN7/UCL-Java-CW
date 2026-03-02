<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/meta.jsp"/>
  <title>Patient Data App - Error</title>
</head>
<body>
<jsp:include page="/header.jsp"/>
<main class="app-shell main-content page-enter">
  <section class="surface-card">
    <h1 class="page-title">Something went wrong</h1>
    <p class="muted">The request could not be completed. Please review the message below.</p>
    <%
      String errorMessage = (String) request.getAttribute("errorMessage");
      if (errorMessage == null || errorMessage.trim().isEmpty())
      {
        errorMessage = "An unexpected error occurred.";
      }
    %>
    <div class="alert-error"><%= errorMessage %></div>
    <div class="error-actions">
      <a class="btn btn-secondary" href="/patientList">Back to patient table</a>
      <a class="btn btn-secondary" href="/">Go to home</a>
    </div>
  </section>
</main>
<jsp:include page="/footer.jsp"/>
</body>
</html>
