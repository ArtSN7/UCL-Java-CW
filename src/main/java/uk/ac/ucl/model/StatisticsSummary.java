package uk.ac.ucl.model;

public record StatisticsSummary(
  PersonMetric oldestLiving,
  PersonMetric youngestLiving,
  PersonMetric oldestOverall,
  PersonMetric youngestOverall,
  int aliveCount,
  int deadCount,
  int totalCount
) {
}
