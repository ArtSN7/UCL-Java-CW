<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Patient Data App - Error</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <h1 class="h3 mb-2">Something went wrong</h1>
      <p class="text-secondary mb-4">The request could not be completed. Please review the message below.</p>
      <%
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage == null || errorMessage.trim().isEmpty())
        {
          errorMessage = "An unexpected error occurred.";
        }
      %>
      <div class="alert alert-danger alert-error" role="alert"><%= errorMessage %></div>
      <div class="d-flex flex-wrap gap-2">
        <a class="btn btn-outline-primary rounded-pill" href="/patientList">Back to patient table</a>
        <a class="btn btn-primary rounded-pill" href="/">Go to home</a>
      </div>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
