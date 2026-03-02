<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Search Patients</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <h2 class="h3 mb-2">Search Patients</h2>
      <p class="text-secondary mb-4">Enter one or more keywords to match against the patient data.</p>
      <form action="/runsearch" method="get" class="row g-3">
        <div class="col-12">
          <label class="form-label" for="searchstring">Search words</label>
          <input
            id="searchstring"
            name="searchstring"
            type="text"
            class="form-control"
            placeholder="Example: Norah104 Massachusetts"
            required
          />
        </div>
        <div class="col-12">
          <button type="submit" class="btn btn-primary rounded-pill px-4">Run Search</button>
        </div>
      </form>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
