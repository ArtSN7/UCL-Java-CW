package uk.ac.ucl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Column {
    private final String name;
    private final ArrayList<String> rows;

    public Column(String name) {
        String normalizedName = Objects.requireNonNull(name, "Column name cannot be null.").trim();

        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be blank.");
        }

        this.name = normalizedName;
        this.rows = new ArrayList<>();
    }

    public Column(String name, List<String> initialRows) {
        this(name);
        for (String value : Objects.requireNonNull(initialRows, "Initial rows cannot be null.")) {
            addRowValue(value);
        }
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return rows.size();
    }

    public String getRowValue(int row) {
        return rows.get(row);
    }

    public void setRowValue(int row, String value) {
        rows.set(row, normalizeValue(value));
    }

    public void addRowValue(String value) {
        rows.add(normalizeValue(value));
    }

    private static String normalizeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
