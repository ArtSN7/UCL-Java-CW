<%@ page import="java.util.List" %>
<%@ page import="uk.ac.ucl.config.AppConstants" %>
<%@ page import="uk.ac.ucl.model.PersonMetric" %>
<%@ page import="uk.ac.ucl.model.StatisticsSummary" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <jsp:include page="/WEB-INF/jsp/partials/meta.jsp"/>
  <title>Statistics</title>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/partials/header.jsp"/>
<main class="container my-4 page-enter">
  <section class="card border-0 shadow-sm data-card mb-4">
    <div class="card-body p-4">
      <h2 class="h3 mb-2">Statistics Dashboard</h2>
      <p class="text-secondary mb-4">Requirement 7 operations with summary metrics and city-based filtering.</p>

      <%
        StatisticsSummary summary = (StatisticsSummary) request.getAttribute("statisticsSummary");
        PersonMetric oldestLiving = summary == null ? null : summary.oldestLiving();
        PersonMetric youngestLiving = summary == null ? null : summary.youngestLiving();
        PersonMetric oldestOverall = summary == null ? null : summary.oldestOverall();
        PersonMetric youngestOverall = summary == null ? null : summary.youngestOverall();
      %>

      <div class="stats-grid mb-4">
        <div class="metric-card">
          <p class="metric-label mb-1">Oldest Living</p>
          <%
            if (oldestLiving != null) {
          %>
          <p class="metric-name mb-1"><strong><%= oldestLiving.firstName() %> <%= oldestLiving.lastName() %></strong></p>
          <p class="mb-1">Age: <%= oldestLiving.ageYears() %></p>
          <p class="mb-2">DOB: <%= oldestLiving.birthDate() %></p>
          <a class="id-link" href="/patient?id=<%= oldestLiving.id() %>">View patient</a>
          <%
            } else {
          %>
          <p class="text-secondary mb-0">Not available</p>
          <%
            }
          %>
        </div>

        <div class="metric-card">
          <p class="metric-label mb-1">Youngest Living</p>
          <%
            if (youngestLiving != null) {
          %>
          <p class="metric-name mb-1"><strong><%= youngestLiving.firstName() %> <%= youngestLiving.lastName() %></strong></p>
          <p class="mb-1">Age: <%= youngestLiving.ageYears() %></p>
          <p class="mb-2">DOB: <%= youngestLiving.birthDate() %></p>
          <a class="id-link" href="/patient?id=<%= youngestLiving.id() %>">View patient</a>
          <%
            } else {
          %>
          <p class="text-secondary mb-0">Not available</p>
          <%
            }
          %>
        </div>

        <div class="metric-card">
          <p class="metric-label mb-1">Oldest Overall</p>
          <%
            if (oldestOverall != null) {
          %>
          <p class="metric-name mb-1"><strong><%= oldestOverall.firstName() %> <%= oldestOverall.lastName() %></strong></p>
          <p class="mb-1">Age: <%= oldestOverall.ageYears() %></p>
          <p class="mb-2">DOB: <%= oldestOverall.birthDate() %></p>
          <a class="id-link" href="/patient?id=<%= oldestOverall.id() %>">View patient</a>
          <%
            } else {
          %>
          <p class="text-secondary mb-0">Not available</p>
          <%
            }
          %>
        </div>

        <div class="metric-card">
          <p class="metric-label mb-1">Youngest Overall</p>
          <%
            if (youngestOverall != null) {
          %>
          <p class="metric-name mb-1"><strong><%= youngestOverall.firstName() %> <%= youngestOverall.lastName() %></strong></p>
          <p class="mb-1">Age: <%= youngestOverall.ageYears() %></p>
          <p class="mb-2">DOB: <%= youngestOverall.birthDate() %></p>
          <a class="id-link" href="/patient?id=<%= youngestOverall.id() %>">View patient</a>
          <%
            } else {
          %>
          <p class="text-secondary mb-0">Not available</p>
          <%
            }
          %>
        </div>
      </div>

      <div class="count-grid mb-4">
        <div class="count-card">
          <p class="metric-label mb-1">Alive</p>
          <p class="display-6 mb-0"><%= summary == null ? 0 : summary.aliveCount() %></p>
        </div>
        <div class="count-card">
          <p class="metric-label mb-1">Dead</p>
          <p class="display-6 mb-0"><%= summary == null ? 0 : summary.deadCount() %></p>
        </div>
        <div class="count-card">
          <p class="metric-label mb-1">Total</p>
          <p class="display-6 mb-0"><%= summary == null ? 0 : summary.totalCount() %></p>
        </div>
      </div>
    </div>
  </section>

  <section class="card border-0 shadow-sm data-card">
    <div class="card-body p-4">
      <h3 class="h4 mb-3">People By City</h3>
      <%
        String selectedCity = (String) request.getAttribute("selectedCity");
        if (selectedCity == null) {
          selectedCity = "";
        }
        String selectedStatus = (String) request.getAttribute("selectedStatus");
        if (selectedStatus == null || selectedStatus.isBlank()) {
          selectedStatus = "all";
        }
        String cityMessage = (String) request.getAttribute("cityMessage");
        List<String> availableCities = (List<String>) request.getAttribute("availableCities");
        List<String> patientNames = (List<String>) request.getAttribute("patientNames");
        Integer resultCount = (Integer) request.getAttribute("resultCount");
        int count = resultCount == null ? 0 : resultCount;
      %>

      <form action="/statistics" method="get" class="row g-3 mb-3">
        <div class="col-md-6">
          <label class="form-label" for="city">City</label>
          <select id="city" name="city" class="form-select">
            <option value="">-- Select city --</option>
            <%
              if (availableCities != null) {
                for (String city : availableCities) {
                  String selected = city.equals(selectedCity) ? "selected" : "";
            %>
            <option value="<%= city %>" <%= selected %>><%= city %></option>
            <%
                }
              }
            %>
          </select>
        </div>

        <div class="col-md-4">
          <label class="form-label" for="status">Status</label>
          <select id="status" name="status" class="form-select">
            <option value="all" <%= "all".equals(selectedStatus) ? "selected" : "" %>>All</option>
            <option value="alive" <%= "alive".equals(selectedStatus) ? "selected" : "" %>>Alive</option>
            <option value="dead" <%= "dead".equals(selectedStatus) ? "selected" : "" %>>Dead</option>
          </select>
        </div>

        <div class="col-md-2 d-grid align-self-end">
          <button type="submit" class="btn btn-primary rounded-pill">Apply</button>
        </div>
      </form>

      <%
        if (selectedCity.isBlank()) {
      %>
      <p class="text-secondary mb-0"><%= AppConstants.Messages.CITY_REQUIRED %></p>
      <%
        } else if (cityMessage != null && !cityMessage.isBlank()) {
      %>
      <div class="alert alert-warning mb-0" role="alert"><%= cityMessage %></div>
      <%
        } else {
      %>
      <p class="mb-3">
        <strong><%= count %></strong> people in <strong><%= selectedCity %></strong> with status
        <strong><%= selectedStatus %></strong>.
      </p>

      <%
          if (patientNames != null && !patientNames.isEmpty()) {
      %>
      <ul class="list-group names-list">
        <%
          for (String patientName : patientNames) {
        %>
        <li class="list-group-item"><%= patientName %></li>
        <%
          }
        %>
      </ul>
      <%
          } else {
      %>
      <p class="text-secondary mb-0">No people found for this filter.</p>
      <%
          }
        }
      %>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
</body>
</html>
