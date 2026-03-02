package uk.ac.ucl.model;

import java.util.*;
import java.util.logging.Logger;

public class DataFrame {
    private static final Logger LOGGER = Logger.getLogger(DataFrame.class.getName());

    private final List<Column> columns;
    private final Map<String, Column> columnsByName;

    public DataFrame() {
        this.columns = new ArrayList<>();
        this.columnsByName = new LinkedHashMap<>();
    }

    public void addColumn(Column column) {
        Column newColumn = Objects.requireNonNull(column, "Column cannot be null.");
        String newColumnName = normalizeColumnName(newColumn.getName());

        if (columnsByName.containsKey(newColumnName)) {
            LOGGER.warning(() -> "Attempted to add duplicate column '" + newColumnName + "'.");
            throw new IllegalArgumentException("Duplicate column name: " + newColumnName);
        }

        if (!columns.isEmpty() && newColumn.getSize() != getRowCount()) {
            LOGGER.warning(() ->
                    "Attempted to add column '" + newColumnName + "' with row count "
                            + newColumn.getSize() + " when expected " + getRowCount() + ".");
            throw new IllegalArgumentException(
                    "Column " + newColumnName + " has " + newColumn.getSize()
                            + " rows but expected " + getRowCount() + ".");
        }

        columns.add(newColumn);
        columnsByName.put(newColumnName, newColumn);
        LOGGER.fine(() -> "Added column '" + newColumnName + "' to DataFrame.");
    }

    public List<String> getColumnNames() {
        List<String> names = new ArrayList<>(columns.size());

        for (Column column : columns) {
            names.add(column.getName());
        }

        return List.copyOf(names);
    }

    public int getRowCount() {
        if (columns.isEmpty()) {
            return 0;
        }
        return columns.get(0).getSize();
    }

    public String getValue(String columnName, int row) {
        return findColumn(columnName).getRowValue(row);
    }

    public void putValue(String columnName, int row, String value) {
        findColumn(columnName).setRowValue(row, value);
    }

    public void addValue(String columnName, String value) {
        findColumn(columnName).addRowValue(value);
    }

    private Column findColumn(String columnName) {
        String normalizedName = normalizeColumnName(columnName);
        Column column = columnsByName.get(normalizedName);
        if (column == null) {
            LOGGER.warning(() -> "Unknown column requested: '" + normalizedName + "'.");
            throw new IllegalArgumentException("Unknown column name: " + normalizedName);
        }
        return column;
    }

    private static String normalizeColumnName(String columnName) {
        String normalizedName =
                Objects.requireNonNull(columnName, "Column name cannot be null.").trim();

        if (normalizedName.isEmpty()) {
            LOGGER.warning("Encountered blank column name.");
            throw new IllegalArgumentException("Column name cannot be blank.");
        }

        return normalizedName;
    }
}
