package uk.ac.ucl.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public DataFrame deepCopy() {
        DataFrame copy = new DataFrame();
        for (Column column : columns) {
            copy.addColumn(column.deepCopy());
        }
        return copy;
    }

    public void appendRow(Map<String, String> rowValuesByColumn) {
        Map<String, String> values = Objects.requireNonNull(rowValuesByColumn, "Row values cannot be null.");
        for (Column column : columns) {
            String columnName = column.getName();
            if (!values.containsKey(columnName)) {
                throw new IllegalArgumentException("Missing value for column: " + columnName);
            }
            column.addRowValue(values.get(columnName));
        }
    }

    public void updateRow(int rowIndex, Map<String, String> rowValuesByColumn) {
        validateRowIndex(rowIndex);
        Map<String, String> values = Objects.requireNonNull(rowValuesByColumn, "Row values cannot be null.");
        for (Column column : columns) {
            String columnName = column.getName();
            if (!values.containsKey(columnName)) {
                throw new IllegalArgumentException("Missing value for column: " + columnName);
            }
            column.setRowValue(rowIndex, values.get(columnName));
        }
    }

    public void removeRow(int rowIndex) {
        validateRowIndex(rowIndex);
        for (Column column : columns) {
            column.removeRowValue(rowIndex);
        }
    }

    public boolean hasColumn(String columnName) {
        String normalizedName = normalizeColumnName(columnName);
        return columnsByName.containsKey(normalizedName);
    }

    public List<Map<String, String>> getRowsForColumns(List<String> selectedColumns) {
        List<String> columnsToRender = normalizeSelectedColumns(selectedColumns);
        int rowCount = getRowCount();
        List<Map<String, String>> rows = new ArrayList<>(rowCount);

        for (int row = 0; row < rowCount; row++) {
            rows.add(getRowForColumns(row, columnsToRender));
        }

        return rows;
    }

    public Map<String, String> getRowForColumns(int row, List<String> selectedColumns) {
        validateRowIndex(row);
        List<String> columnsToRender = normalizeSelectedColumns(selectedColumns);
        Map<String, String> rowData = new LinkedHashMap<>();
        for (String columnName : columnsToRender) {
            rowData.put(columnName, getValueOrEmpty(columnName, row));
        }
        return rowData;
    }

    public Map<String, String> getRecordDetails(int row) {
        validateRowIndex(row);
        Map<String, String> details = new LinkedHashMap<>();
        for (Column column : columns) {
            String value = column.getRowValue(row);
            details.put(column.getName(), value == null ? "" : value);
        }
        return details;
    }

    public List<String> getRowValuesInColumnOrder(int row) {
        validateRowIndex(row);
        List<String> rowValues = new ArrayList<>(columns.size());
        for (Column column : columns) {
            String value = column.getRowValue(row);
            rowValues.add(value == null ? "" : value);
        }
        return rowValues;
    }

    public int findRowByValue(String columnName, String searchValue) {
        if (searchValue == null || searchValue.isBlank()) {
            throw new IllegalArgumentException("Search value cannot be blank.");
        }

        if (!hasColumn(columnName)) {
            return -1;
        }

        String normalizedSearchValue = searchValue.trim();
        Column targetColumn = findColumn(columnName);
        for (int row = 0; row < targetColumn.getSize(); row++) {
            String rowValue = targetColumn.getRowValue(row);
            if (normalizedSearchValue.equals(rowValue == null ? "" : rowValue)) {
                return row;
            }
        }
        return -1;
    }

    public String getValueOrEmpty(String columnName, int row) {
        if (!hasColumn(columnName)) {
            return "";
        }
        String value = getValue(columnName, row);
        return value == null ? "" : value;
    }

    public List<String> normalizeSelectedColumns(List<String> selectedColumns) {
        Objects.requireNonNull(selectedColumns, "Selected columns cannot be null.");
        List<String> normalizedColumns = new ArrayList<>(selectedColumns.size());
        for (String selectedColumn : selectedColumns) {
            normalizedColumns.add(normalizeColumnName(selectedColumn));
        }
        return List.copyOf(normalizedColumns);
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

    private void validateRowIndex(int row) {
        if (row < 0 || row >= getRowCount()) {
            throw new IllegalArgumentException("Row index out of range: " + row);
        }
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
