package de.uni_marburg.schematch.data;

import lombok.Data;

import java.util.List;

@Data
public class Table {
    private final String name;
    private final List<String> labels;
    private List<Column> columns;

    public Table(String name, List<String> labels, List<Column> columns) {
        this.name = name;
        this.labels = labels;
        this.columns = columns;

        for (Column column : this.columns) {
            column.setTable(this);
        }
    }

    public int getNumberOfColumns() {
        return this.columns.size();
    }

    public Column getColumn(int n) {
        return this.columns.get(n);
    }
}