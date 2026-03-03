package uk.ac.ucl.model;

import java.util.Locale;

public enum LifeStatusFilter {
  ALL,
  ALIVE,
  DEAD;

  public static LifeStatusFilter from(String value) {
    if (value == null || value.isBlank()) {
      return ALL;
    }

    return switch (value.trim().toLowerCase(Locale.ROOT)) {
      case "alive" -> ALIVE;
      case "dead" -> DEAD;
      default -> ALL;
    };
  }

  public String toRequestValue() {
    return name().toLowerCase(Locale.ROOT);
  }
}
