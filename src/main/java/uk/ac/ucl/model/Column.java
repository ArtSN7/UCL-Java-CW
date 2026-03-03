package uk.ac.ucl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Column {
    private static final Logger LOGGER = Logger.getLogger(Column.class.getName());

    private final String name;
    private final ArrayList<String> rows;

    public Column(String name) {
        String normalizedName = Objects.requireNonNull(name, "Column name cannot be null.").trim();

        if (normalizedName.isEmpty()) {
            LOGGER.warning("Attempted to create a column with a blank name.");
            throw new IllegalArgumentException("Column name cannot be blank.");
        }

        this.name = normalizedName;
        this.rows = new ArrayList<>();
        LOGGER.fine(() -> "Created column '" + this.name + "'.");
    }

    public Column(String name, List<String> initialRows) {
        this(name);
        for (String value : Objects.requireNonNull(initialRows, "Initial rows cannot be null.")) {
            addRowValue(value);
        }
        LOGGER.fine(() -> "Initialized column '" + this.name + "' with " + rows.size() + " rows.");
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

    public void removeRowValue(int row) {
        rows.remove(row);
    }

    public Column deepCopy() {
        return new Column(name, rows);
    }

    private static String normalizeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
