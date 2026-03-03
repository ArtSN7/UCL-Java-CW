<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="uk.ac.ucl.config.AppConstants" %>
<%@ page import="uk.ac.ucl.model.PersonMetric" %>
<%@ page import="uk.ac.ucl.model.StatisticsSummary" %>
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

<%
  StatisticsSummary summary = (StatisticsSummary) request.getAttribute("statisticsSummary");
  PersonMetric oldestLiving = summary == null ? null : summary.oldestLiving();
  PersonMetric youngestLiving = summary == null ? null : summary.youngestLiving();
  PersonMetric oldestOverall = summary == null ? null : summary.oldestOverall();
  PersonMetric youngestOverall = summary == null ? null : summary.youngestOverall();

  String selectedCity = (String) request.getAttribute("selectedCity");
  if (selectedCity == null) {
    selectedCity = "";
  }

  String selectedStatus = (String) request.getAttribute("selectedStatus");
  if (selectedStatus == null || selectedStatus.isBlank()) {
    selectedStatus = "all";
  }

  String selectedStatusLabel = (String) request.getAttribute("selectedStatusLabel");
  if (selectedStatusLabel == null || selectedStatusLabel.isBlank()) {
    selectedStatusLabel = "All";
  }

  String cityMessage = (String) request.getAttribute("cityMessage");

  List<String> availableCities = (List<String>) request.getAttribute("availableCities");
  if (availableCities == null) {
    availableCities = List.of();
  }

  List<String> patientNames = (List<String>) request.getAttribute("patientNames");
  if (patientNames == null) {
    patientNames = List.of();
  }

  Integer resultCount = (Integer) request.getAttribute("resultCount");
  int count = resultCount == null ? 0 : resultCount;

  List<String> ageLabels = (List<String>) request.getAttribute("ageLabels");
  if (ageLabels == null) {
    ageLabels = List.of();
  }

  List<String> ethnicityLabels = (List<String>) request.getAttribute("ethnicityLabels");
  if (ethnicityLabels == null) {
    ethnicityLabels = List.of();
  }

  List<String> raceLabels = (List<String>) request.getAttribute("raceLabels");
  if (raceLabels == null) {
    raceLabels = List.of();
  }

  String ageLabelsJson = (String) request.getAttribute("ageLabelsJson");
  if (ageLabelsJson == null) {
    ageLabelsJson = "[]";
  }

  String ageCountsJson = (String) request.getAttribute("ageCountsJson");
  if (ageCountsJson == null) {
    ageCountsJson = "[]";
  }

  String ethnicityLabelsJson = (String) request.getAttribute("ethnicityLabelsJson");
  if (ethnicityLabelsJson == null) {
    ethnicityLabelsJson = "[]";
  }

  String ethnicityCountsJson = (String) request.getAttribute("ethnicityCountsJson");
  if (ethnicityCountsJson == null) {
    ethnicityCountsJson = "[]";
  }

  String raceLabelsJson = (String) request.getAttribute("raceLabelsJson");
  if (raceLabelsJson == null) {
    raceLabelsJson = "[]";
  }

  String raceCountsJson = (String) request.getAttribute("raceCountsJson");
  if (raceCountsJson == null) {
    raceCountsJson = "[]";
  }
%>

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

      <div class="stats-grid mb-4">
        <div class="metric-card">
          <p class="metric-label mb-1">Oldest Living</p>
          <%
            if (oldestLiving != null) {
          %>
          <p class="metric-name mb-1">
            <strong><%= escapeHtml(oldestLiving.firstName()) %> <%= escapeHtml(oldestLiving.lastName()) %></strong>
          </p>
          <p class="mb-1">Age: <%= oldestLiving.ageYears() %></p>
          <p class="mb-2">DOB: <%= escapeHtml(oldestLiving.birthDate()) %></p>
          <a class="id-link" href="/patient?id=<%= encodeUrlParam(oldestLiving.id()) %>">View patient</a>
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
          <p class="metric-name mb-1">
            <strong><%= escapeHtml(youngestLiving.firstName()) %> <%= escapeHtml(youngestLiving.lastName()) %></strong>
          </p>
          <p class="mb-1">Age: <%= youngestLiving.ageYears() %></p>
          <p class="mb-2">DOB: <%= escapeHtml(youngestLiving.birthDate()) %></p>
          <a class="id-link" href="/patient?id=<%= encodeUrlParam(youngestLiving.id()) %>">View patient</a>
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
          <p class="metric-name mb-1">
            <strong><%= escapeHtml(oldestOverall.firstName()) %> <%= escapeHtml(oldestOverall.lastName()) %></strong>
          </p>
          <p class="mb-1">Age: <%= oldestOverall.ageYears() %></p>
          <p class="mb-2">DOB: <%= escapeHtml(oldestOverall.birthDate()) %></p>
          <a class="id-link" href="/patient?id=<%= encodeUrlParam(oldestOverall.id()) %>">View patient</a>
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
          <p class="metric-name mb-1">
            <strong><%= escapeHtml(youngestOverall.firstName()) %> <%= escapeHtml(youngestOverall.lastName()) %></strong>
          </p>
          <p class="mb-1">Age: <%= youngestOverall.ageYears() %></p>
          <p class="mb-2">DOB: <%= escapeHtml(youngestOverall.birthDate()) %></p>
          <a class="id-link" href="/patient?id=<%= encodeUrlParam(youngestOverall.id()) %>">View patient</a>
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

      <form action="/statistics" method="get" class="row g-3 mb-3">
        <div class="col-md-6">
          <label class="form-label" for="city">City</label>
          <select id="city" name="city" class="form-select">
            <option value="">-- Select city --</option>
            <%
              for (String city : availableCities) {
                String selected = city.equals(selectedCity) ? "selected" : "";
            %>
            <option value="<%= escapeHtml(city) %>" <%= selected %>><%= escapeHtml(city) %></option>
            <%
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
      <p class="text-secondary mb-0"><%= escapeHtml(AppConstants.Messages.CITY_REQUIRED) %></p>
      <%
        } else if (cityMessage != null && !cityMessage.isBlank()) {
      %>
      <div class="alert alert-warning mb-0" role="alert"><%= escapeHtml(cityMessage) %></div>
      <%
        } else {
      %>
      <p class="mb-3">
        <strong><%= count %></strong> people in <strong><%= escapeHtml(selectedCity) %></strong> with status
        <strong><%= escapeHtml(selectedStatusLabel) %></strong>.
      </p>

      <%
          if (!patientNames.isEmpty()) {
      %>
      <ul class="list-group names-list">
        <%
          for (String patientName : patientNames) {
        %>
        <li class="list-group-item"><%= escapeHtml(patientName) %></li>
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

  <section class="card border-0 shadow-sm data-card mt-4">
    <div class="card-body p-4">
      <h3 class="h4 mb-2">Demographic Distributions</h3>
      <p class="text-secondary mb-4">Requirement 10 charts: age distribution, ethnicity share, and race share.</p>

      <div class="chart-grid">
        <div class="chart-card chart-card-wide">
          <h4 class="h5 mb-3">Age Distribution</h4>
          <%
            if (ageLabels.isEmpty()) {
          %>
          <p class="text-secondary mb-0">No valid age data available for charting.</p>
          <%
            } else {
          %>
          <div class="chart-canvas-wrap">
            <canvas id="ageDistributionChart"></canvas>
          </div>
          <%
            }
          %>
        </div>

        <div class="chart-card">
          <h4 class="h5 mb-3">Ethnicity Distribution</h4>
          <%
            if (ethnicityLabels.isEmpty()) {
          %>
          <p class="text-secondary mb-0">No ethnicity data available for charting.</p>
          <%
            } else {
          %>
          <div class="chart-canvas-wrap">
            <canvas id="ethnicityDistributionChart"></canvas>
          </div>
          <%
            }
          %>
        </div>

        <div class="chart-card">
          <h4 class="h5 mb-3">Race Distribution</h4>
          <%
            if (raceLabels.isEmpty()) {
          %>
          <p class="text-secondary mb-0">No race data available for charting.</p>
          <%
            } else {
          %>
          <div class="chart-canvas-wrap">
            <canvas id="raceDistributionChart"></canvas>
          </div>
          <%
            }
          %>
        </div>
      </div>
    </div>
  </section>
</main>
<jsp:include page="/WEB-INF/jsp/partials/footer.jsp"/>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js"></script>
<script>
  (() => {
    if (!window.Chart) {
      return;
    }

    const ageLabels = <%= ageLabelsJson %>;
    const ageCounts = <%= ageCountsJson %>;
    const ethnicityLabels = <%= ethnicityLabelsJson %>;
    const ethnicityCounts = <%= ethnicityCountsJson %>;
    const raceLabels = <%= raceLabelsJson %>;
    const raceCounts = <%= raceCountsJson %>;

    const palette = [
      "#1f77b4", "#17becf", "#2ca02c", "#ff7f0e", "#d62728",
      "#9467bd", "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22"
    ];

    const makeColors = (size) => Array.from({length: size}, (_, index) => palette[index % palette.length]);

    if (ageLabels.length > 0 && ageCounts.length > 0) {
      new Chart(document.getElementById("ageDistributionChart"), {
        type: "bar",
        data: {
          labels: ageLabels,
          datasets: [{
            label: "People",
            data: ageCounts,
            backgroundColor: "rgba(31, 119, 180, 0.68)",
            borderColor: "#1f77b4",
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {display: false}
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {precision: 0}
            },
            x: {
              title: {
                display: true,
                text: "Age (years)"
              }
            }
          }
        }
      });
    }

    const renderPie = (canvasId, labels, values) => {
      if (labels.length === 0 || values.length === 0) {
        return;
      }

      new Chart(document.getElementById(canvasId), {
        type: "pie",
        data: {
          labels,
          datasets: [{
            data: values,
            backgroundColor: makeColors(labels.length),
            borderColor: "#ffffff",
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "bottom"
            }
          }
        }
      });
    };

    renderPie("ethnicityDistributionChart", ethnicityLabels, ethnicityCounts);
    renderPie("raceDistributionChart", raceLabels, raceCounts);
  })();
</script>
</body>
</html>
