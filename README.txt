COMP0004 Coursework Submission Summary

This web application implements a patient dashboard using Java, servlets, JSP, and an MVC structure.
The model layer loads patient CSV data into a custom DataFrame/Column structure and exposes all data
operations through a singleton Model class used by the controllers.

Requirement coverage
1) Column class: Implemented with name + row storage and methods to get/set/add row values.
2) DataFrame class: Implemented with addColumn, getColumnNames, getRowCount, getValue, putValue,
   addValue, and supporting row operations for app use.
3) DataLoader: Implemented CSV loading into DataFrame, including header parsing, malformed row handling,
   and CSV saving after data edits.
4) Model: Implemented as singleton. Owns DataFrame, DataLoader, JSONWriter, and all app logic.
5) Servlets/JSPs: Multiple endpoints and pages for listing, viewing, adding, editing, deleting, searching,
   exporting, and statistics.
6) Search: Implemented in Model (keyword-based matching across row data). Servlet only coordinates request/response.
7) Additional operations: Implemented oldest/youngest people (living and overall), alive/dead totals,
   and city-based filtered name lists.
8) Add/Edit/Delete + CSV persistence: Implemented with form workflows and writes back to CSV.
9) JSONWriter + JSON export option: Implemented dedicated JSONWriter class that writes DataFrame snapshots
   to JSON files; export endpoint provides downloadable JSON output.
10) Graphs/charts: Implemented age, ethnicity, and race distributions on the statistics page (Chart.js).

Additional notes
- Data files are stored under /data (not under /src).
- Logging is used instead of System.out output for runtime diagnostics.
- Dynamic page output is HTML-escaped to reduce injection risk.
- Project builds with Maven (`mvn clean package`) and runs with embedded Tomcat (`mvn clean compile exec:exec`).
