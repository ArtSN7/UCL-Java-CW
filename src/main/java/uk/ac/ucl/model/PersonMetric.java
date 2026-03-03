package uk.ac.ucl.model;

public record PersonMetric(
  String id,
  String firstName,
  String lastName,
  String birthDate,
  String deathDate,
  int ageYears,
  boolean alive
) {
}
